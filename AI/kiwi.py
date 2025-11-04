# -*- coding: utf-8 -*-
# KRKeywordClassic v2 — Kiwi only (명사/복합어 + PMI), 잡음 강제 제거 & 병합 강화

import re
import math
from collections import Counter, defaultdict
from typing import List, Tuple
from kiwipiepy import Kiwi

# -----------------------------
# 0) 사전/패턴
# -----------------------------
STOPWORDS = {
    # UI/플랫폼 잡음
    "이미지","사진","그림","화면","스크린샷","상단","하단","오른쪽","왼쪽",
    "상태","표시줄","배터리","시간","와이파이","wifi","wi","fi","연결",
    "사용자","인터페이스","요소","텍스트","그래픽","위치","정보","제외","추가",
    "모바일","휴대폰","앱","온라인","쇼핑","경험","표시","팔로우","아이디",
    "url","http","https","리뷰","참조","상세설명","현재","판매중","판매","혜택","플러스",
    "가을","한정","원산지","최대","적립","포인트","원","광고","라벨","로고",
    # 숫자성 토큰이랑 붙으면 노이즈 유발
    "ad","hd","kt","talk","km"
}

# 토큰 내부에 보이면 잡음 취급 (약어/패널/계기판 숫자)
RE_ALLCAPS = re.compile(r"^[A-Z]{2,}$")
RE_MIXED   = re.compile(r"^(?:[A-Za-z]+\d+|\d+[A-Za-z]+)$")
RE_UNIT    = re.compile(r"^\d+\s?(?:g|kg|ml|l|개|팩|봉|박스|캔|병|묶음|세트|매|장|gx|x\d+)$", re.I)
RE_HAN     = re.compile(r"[가-힣]")
RE_VALID   = re.compile(r"[가-힣A-Za-z0-9]+")

# 흔한 ‘분리 표기’를 붙여주는 병합 규칙(필요시 계속 추가)
JOIN_RULES = {
    ("초코","송이"): "초코송이",
    ("초코칩","쿠키"): "초코칩 쿠키",
    ("애플","파이"): "애플파이",
    ("밀크","티"): "밀크티",
    ("크로","아상"): "크로아상",
    ("델리","프랑스"): "델리프랑스",
    ("로얄","밀크티"): "로얄 밀크티",
}

# 한글 표기 변형 정규화(공백 제거 기준)
CANON_REPLACEMENTS = [
    ("  ", " "),
]

def canon_form(s: str) -> str:
    s = s.strip().lower()
    for a,b in CANON_REPLACEMENTS:
        while a in s: s = s.replace(a,b)
    # 한글만인 경우는 공백 제거한 버전도 키로 씀
    if RE_HAN.search(s) and not re.search(r"[A-Za-z0-9]", s):
        return s.replace(" ","")
    return s

def is_noise_token(tok: str) -> bool:
    t = tok.strip().lower()
    if len(t) <= 1 and not t.isdigit(): return True
    if t in STOPWORDS: return True
    if RE_ALLCAPS.match(t): return True
    if RE_MIXED.match(t): return True
    if RE_UNIT.match(t): return True
    if not RE_VALID.search(t): return True
    return False

def brand_like(tok: str) -> bool:
    # 브랜드/상품 힌트(대문자/영문혼합은 노이즈도 되지만, 한글+고유명 경향 보너스만 약하게)
    return bool(RE_HAN.search(tok)) and (len(tok) >= 2)

# -----------------------------
# 1) 토큰화 (명사/고유명사/외래어)
# -----------------------------
def tokenize_kiwi(kiwi: Kiwi, text: str) -> List[str]:
    text = text.replace("\u200b"," ").replace("\ufeff"," ")
    text = re.sub(r"\s+", " ", text).strip()
    toks: List[str] = []
    for sent in kiwi.analyze(text):
        for t in sent[0]:
            if t.tag.startswith(("NN",)) or t.tag == "SL":
                w = t.form.strip()
                if w and RE_VALID.search(w):
                    toks.append(w)
    return toks

# 인접 토큰 병합 규칙 적용
def apply_join_rules(tokens: List[str]) -> List[str]:
    i, out = 0, []
    L = len(tokens)
    while i < L:
        if i < L-1 and (tokens[i], tokens[i+1]) in JOIN_RULES:
            out.append(JOIN_RULES[(tokens[i], tokens[i+1])])
            i += 2
        else:
            out.append(tokens[i])
            i += 1
    return out

def build_ngrams(tokens: List[str], n: int) -> List[Tuple[str,...]]:
    return [tuple(tokens[i:i+n]) for i in range(len(tokens)-n+1)]

# -----------------------------
# 2) 핵심 로직
# -----------------------------
def best_keywords(text: str, top_k: int = 15,
                  min_unigram=2, min_bigram=2,
                  pmi_floor=0.2) -> List[Tuple[str, float]]:
    kiwi = Kiwi()
    # 1) 토큰화 + 불용어 컷
    raw_tokens = tokenize_kiwi(kiwi, text)
    raw_tokens = [w for w in raw_tokens if not is_noise_token(w)]

    # 2) 인접 병합(초코 송이→초코송이 등)
    tokens = apply_join_rules(raw_tokens)
    tokens = [w for w in tokens if not is_noise_token(w)]
    if not tokens:
        return []

    # 3) 유니그램/바이그램 빈도
    uni = Counter(tokens)
    bi  = Counter([bg for bg in build_ngrams(tokens, 2)
                   if not (is_noise_token(bg[0]) or is_noise_token(bg[1]))])

    # 최소 빈도 필터(잡음 결합 억제)
    uni = Counter({w:c for w,c in uni.items() if c >= min_unigram})
    bi  = Counter({bg:c for bg,c in bi.items() if c >= min_bigram})

    total_uni = sum(uni.values()) or 1
    total_bi  = sum(bi.values()) or 1

    # 4) PMI 계산 (하한선 적용)
    pmi = {}
    for (w1,w2), c12 in bi.items():
        p_w1 = uni.get(w1,0)/total_uni
        p_w2 = uni.get(w2,0)/total_uni
        p_w1w2 = c12/total_bi
        score = 0.0
        if p_w1>0 and p_w2>0 and p_w1w2>0:
            score = math.log2(p_w1w2/(p_w1*p_w2))
        if score >= pmi_floor:
            pmi[(w1,w2)] = score

    # 5) 스코어링
    cand_scores: dict[str,float] = defaultdict(float)

    # (a) 바이그램 우선: 빈도 + PMI + 보너스/페널티
    for (w1,w2), c12 in bi.items():
        if (w1,w2) not in pmi:
            continue  # PMI 하한 미달 컷
        phrase = f"{w1} {w2}"
        score = 0.0
        score += c12 * 1.0                     # 빈도
        score += pmi[(w1,w2)] * 1.3            # 결합 보너스
        if brand_like(w1): score += 0.2
        if brand_like(w2): score += 0.2

        # 영문 약어/숫자 위주면 페널티
        if not RE_HAN.search(phrase):
            score -= 0.8
        cand_scores[phrase] = max(cand_scores[phrase], score)

    # (b) 유니그램: 바이그램 대비 낮게 반영
    for w, c in uni.items():
        score = c * 0.7
        if brand_like(w): score += 0.2
        if not RE_HAN.search(w):
            score -= 0.6
        cand_scores[w] = max(cand_scores[w], score)

    # 6) 정규화 키로 중복/변형 통합 (“초코 송이” vs “초코송이”)
    fused: dict[str, Tuple[str,float]] = {}  # canon_key -> (repr, score)
    for k, s in cand_scores.items():
        key = canon_form(k)
        if key not in fused or s > fused[key][1]:
            fused[key] = (k, s)

    # 7) 최종 정렬 + ‘구성어 포함도’로 중복 억제
    ranked = sorted(fused.values(), key=lambda x: x[1], reverse=True)
    selected: List[Tuple[str,float]] = []
    used_parts = set()

    for kw, sc in ranked:
        parts = tuple(kw.split())
        # (한글 기준) 구성어가 전부 이미 사용되면 스킵
        if all(canon_form(p) in used_parts for p in parts if RE_HAN.search(p)):
            continue
        selected.append((kw, sc))
        for p in parts:
            used_parts.add(canon_form(p))
        if len(selected) >= top_k:
            break

    return selected

# -----------------------------
# CLI
# -----------------------------
if __name__ == "__main__":
    print("🧠 텍스트를 붙여넣고 엔터 2번으로 종료하세요.")
    lines = []
    while True:
        try:
            line = input()
        except EOFError:
            break
        if not line.strip():
            break
        lines.append(line)
    text = "\n".join(lines).strip()

    kws = best_keywords(text, top_k=15)
    if not kws:
        print("키워드를 찾지 못했습니다.")
    else:
        print("\n✅ 키워드 (상위 15개):")
        for k, s in kws:
            print(f"  {k:25s} {s:.3f}")
