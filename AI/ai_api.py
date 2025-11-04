import requests
import base64
import time
from io import BytesIO
from PIL import Image
import numpy as np
from pathlib import Path
from paddleocr import PaddleOCRVL
from fastapi import FastAPI, UploadFile, File
from pydantic import BaseModel
import uvicorn

# ==============================
# 🔧 설정
# ==============================
OLLAMA_URL = "http://localhost:11434/api/generate"
LLAVA_MODEL = "llava:7b"       # 이미지 설명용
TRANSLATE_MODEL = "gemma3:4b"  # 한국어 번역용
MAX_SIZE = 1024                # 긴 변 리사이즈 제한(px)

# ==============================
# PaddleOCR-VL 초기화
# ==============================
print("🔄 PaddleOCR-VL 모델 로딩 중...")
ocr_pipeline = PaddleOCRVL()
print("✅ PaddleOCR-VL 모델 로딩 완료!")

# ==============================
# FastAPI 앱 초기화
# ==============================
app = FastAPI(title="Image Analysis API")

# ==============================
# 입력 모델 (URL)
# ==============================
class ImageURL(BaseModel):
    url: str

# ==============================
# 🧩 이미지 로드 + 리사이즈
# ==============================
def load_and_resize_image_from_bytes(img_bytes, max_size=MAX_SIZE):
    img = Image.open(BytesIO(img_bytes)).convert("RGB")
    w, h = img.size
    if max(w, h) > max_size:
        scale = max_size / max(w, h)
        img = img.resize((int(w * scale), int(h * scale)), Image.LANCZOS)
    return img

def load_and_resize_image_from_url(url, max_size=MAX_SIZE):
    resp = requests.get(url)
    if resp.status_code != 200:
        raise RuntimeError(f"이미지 다운로드 실패: {resp.status_code}")
    return load_and_resize_image_from_bytes(resp.content, max_size=max_size)

# ==============================
# 🧠 LLaVA + Gemma3 처리
# ==============================
def run_llava_and_translate(img):
    start_time = time.time()

    # 이미지 → Base64
    buffer = BytesIO()
    img.save(buffer, format="JPEG", quality=85)
    img_b64 = base64.b64encode(buffer.getvalue()).decode("utf-8")

    # LLaVA 영어 설명 요청
    payload_llava = {
        "model": LLAVA_MODEL,
        "prompt": "Describe this image shortly",
        "images": [img_b64],
        "stream": False
    }
    res1 = requests.post(OLLAMA_URL, json=payload_llava)
    if not res1.ok:
        raise RuntimeError(f"LLaVA 요청 실패: {res1.status_code}\n{res1.text}")
    english_desc = res1.json()["response"].strip()

    # Gemma3 번역 요청
    payload_trans = {
        "model": TRANSLATE_MODEL,
        "prompt": f"내가 보낸 문장만 한국어로 번역해줘 다른말은 추가하지마 {english_desc}",
        "stream": False
    }
    res2 = requests.post(OLLAMA_URL, json=payload_trans)
    if not res2.ok:
        raise RuntimeError(f"Gemma3 요청 실패: {res2.status_code}\n{res2.text}")
    korean_desc = res2.json()["response"].strip()

    print(f"✅ LLaVA 완료 ({round(time.time() - start_time, 2)}초)")
    return korean_desc


# ==============================
# 🧾 PaddleOCR 처리
# ==============================
def safe_serialize(obj):
    """JSON 직렬화 가능한 형태로 변환"""
    if isinstance(obj, (str, int, float, bool)) or obj is None:
        return obj
    if isinstance(obj, (list, tuple, set)):
        return [safe_serialize(i) for i in obj]
    if isinstance(obj, dict):
        return {str(k): safe_serialize(v) for k, v in obj.items()}
    if isinstance(obj, np.generic):
        return obj.item()
    if hasattr(obj, "__dict__"):
        return safe_serialize(obj.__dict__)
    return str(obj)

def extract_text_blocks(res_dict):
    """PaddleOCR-VL 결과 JSON에서 텍스트 블록만 추출"""
    texts = []
    for block in res_dict.get("parsing_res_list", []):
        content = block.get("content", "").strip()
        if content:
            texts.append({
                "label": block.get("label", "unknown"),
                "bbox": block.get("bbox", []),
                "content": content
            })
    return texts

def run_ocr(img):
    """OCR 수행 후 블록별 텍스트와 합쳐진 결과 반환"""
    start_time = time.time()
    img_np = np.array(img)
    result_list = []

    try:
        output = ocr_pipeline.predict(img_np)

        for idx, res in enumerate(output):
            res_dict = safe_serialize(res)
            texts = extract_text_blocks(res_dict)

            if not texts:
                continue

            combined_text = " ".join([t["content"] for t in texts])
            result_list.append(combined_text)

    except Exception as e:
        print(f"⚠️ OCR 처리 중 오류 발생: {e}")

    total_time = round(time.time() - start_time, 2)
    print(f"✅ OCR 완료 ({total_time}초, {len(result_list)}개 블록)")

    # 블록 구분 줄바꿈으로 이어붙임
    return "\n".join(result_list)


# ==============================
# 🚀 FastAPI 엔드포인트
# ==============================
@app.post("/analyze_file")
async def analyze_file(file: UploadFile = File(...)):
    img_bytes = await file.read()
    img = load_and_resize_image_from_bytes(img_bytes)
    llava_korean = run_llava_and_translate(img)
    ocr_text = run_ocr(img)
    return {"llava": llava_korean, "ocr_text": ocr_text}

@app.post("/analyze_url")
async def analyze_url(data: ImageURL):
    img = load_and_resize_image_from_url(data.url)
    llava_korean = run_llava_and_translate(img)
    ocr_text = run_ocr(img)
    return {"llava": llava_korean, "ocr_text": ocr_text}


# ==============================
# ▶️ 실행
# ==============================
if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)