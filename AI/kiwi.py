# -*- coding: utf-8 -*-
# KRKeywordClassic v2 — YAML 외부 사전 로딩 (Kiwi 명사/복합어 + PMI)
# - 화이트리스트: binary set (가중치 없음, 고정 보너스)
# - Stopwords: 공통 리스트
# - Join rules: 예외 몇 가지만 수동, 나머지는 화이트리스트 기반 자동 병합

import os
import re
import math
import yaml
from collections import Counter, defaultdict
from typing import List, Tuple
from kiwipiepy import Kiwi

# =============================================================================
# 0) 정규식 패턴
# =============================================================================
RE_ALLCAPS = re.compile(r"^[A-Z]{2,}$")
RE_MIXED   = re.compile(r"^(?:[A-Za-z]+\d+|\d+[A-Za-z]+)$")
RE_UNIT    = re.compile(r"^\d+\s?(?:g|kg|ml|l|개|팩|봉|박스|캔|병|묶음|세트|매|장|gx|x\d+)$", re.I)
RE_HAN     = re.compile(r"[가-힣]")
RE_VALID   = re.compile(r"[가-힣A-Za-z0-9]+")

# 정규화(공백 축약 등)
CANON_REPLACEMENTS = [("  ", " ")]

def canon_form(s: str) -> str:
    s = s.strip().lower()
    for a, b in CANON_REPLACEMENTS:
        while a in s:
            s = s.replace(a, b)
    # 한글만인 경우 공백 제거 버전 사용
    if RE_HAN.search(s) and not re.search(r"[A-Za-z0-9]", s):
        return s.replace(" ", "")
    return s

# =============================================================================
# 1) YAML 로더
# =============================================================================
DICT_DIR = os.environ.get("DICT_DIR", os.path.join(os.path.dirname(__file__), "dicts"))

STOPWORDS_SET: set[str] = set()
WHITELIST_SET: set[str] = set()
JOIN_RULES: dict[tuple[str, ...], str] = {}

# 화이트리스트 우대(가중치 대신 고정 보너스)
WL_BONUS = 1.0
FORCE_INCLUDE_WHITELIST = True  # 빈도/PMI 없어도 상단 노출에 유리하게

def _read_yaml(path: str) -> dict:
    with open(path, "r", encoding="utf-8") as f:
        return yaml.safe_load(f) or {}

def load_dictionaries(dict_dir: str = DICT_DIR):
    """dicts/stopwords.yaml, whitelist.yaml, join_rules.yaml 로드"""
    global STOPWORDS_SET, WHITELIST_SET, JOIN_RULES

    # stopwords.yaml
    sw = _read_yaml(os.path.join(dict_dir, "stopwords.yaml"))
    STOPWORDS_SET = set(map(str, (sw.get("common") or [])))

    # whitelist.yaml
    wl = _read_yaml(os.path.join(dict_dir, "whitelist.yaml"))
    WHITELIST_SET = set()
    for section in ("food", "brand", "place", "generic"):
        for tok in (wl.get(section) or []):
            # 문자열만 지원 (심플 스키마)
            if isinstance(tok, str):
                WHITELIST_SET.add(tok.strip())
            else:
                # 혹시 객체가 들어와도 token 키만 사용 (호환)
                token = str(tok.get("token", "")).strip()
                if token:
                    WHITELIST_SET.add(token)

    # join_rules.yaml
    jr = _read_yaml(os.path.join(dict_dir, "join_rules.yaml"))
    JOIN_RULES = {}
    for r in (jr.get("rules") or []):
        inp = tuple(map(str, r.get("in") or []))
        out = str(r.get("out") or "").strip()
        if inp and out:
            JOIN_RULES[inp] = out

# 최초 1회 로드
load_dictionaries()

# =============================================================================
# 2) 유틸
# =============================================================================
def whitelist_hit(tok: str) -> bool:
    """정규형 토큰이 화이트리스트에 있는지 여부"""
    return tok in WHITELIST_SET

def is_noise_token(tok: str) -> bool:
    """불용어/패턴 컷"""
    t = tok.strip().lower()
    if len(t) <= 1 and not t.isdigit(): return True
    if t in STOPWORDS_SET: return True
    if RE_ALLCAPS.match(t): return True
    if RE_MIXED.match(t): return True
    if RE_UNIT.match(t): return True
    if not RE_VALID.search(t): return True
    return False

def brand_like(tok: str) -> bool:
    """한글 포함 & 길이>=2 → 고유명/일반명 경향 보너스"""
    return bool(RE_HAN.search(tok)) and (len(tok) >= 2)

# =============================================================================
# 3) 토큰화 & 병합
# =============================================================================
def tokenize_kiwi(kiwi: Kiwi, text: str) -> List[str]:
    """Kiwi로 명사/외래어 위주 토큰화"""
    text = text.replace("\u200b", " ").replace("\ufeff", " ")
    text = re.sub(r"\s+", " ", text).strip()
    toks: List[str] = []
    for sent in kiwi.analyze(text):
        for t in sent[0]:
            if t.tag.startswith(("NN",)) or t.tag == "SL":
                w = t.form.strip()
                if w and RE_VALID.search(w):
                    toks.append(w)
    return toks

def apply_join_rules(tokens: List[str]) -> List[str]:
    """YAML에 정의된 예외적 병합 규칙 적용 (가변 길이 지원)"""
    i, out = 0, []
    L = len(tokens)
    max_n = max((len(k) for k in JOIN_RULES.keys()), default=2)
    while i < L:
        matched = False
        for n in range(min(max_n, L - i), 1, -1):
            tup = tuple(tokens[i:i+n])
            if tup in JOIN_RULES:
                out.append(JOIN_RULES[tup])
                i += n
                matched = True
                break
        if not matched:
            out.append(tokens[i])
            i += 1
    return out

def auto_join_by_whitelist(tokens: List[str]) -> List[str]:
    """연속 토큰(최대 4그램)을 붙여본 결과가 화이트리스트면 자동 병합"""
    out, i, L = [], 0, len(tokens)
    while i < L:
        merged = None
        for n in range(min(4, L - i), 1, -1):
            cand = "".join(tokens[i:i+n])
            if cand in WHITELIST_SET:
                merged = cand
                i += n
                break
        if merged:
            out.append(merged)
        else:
            out.append(tokens[i])
            i += 1
    return out

def build_ngrams(tokens: List[str], n: int) -> List[Tuple[str, ...]]:
    return [tuple(tokens[i:i+n]) for i in range(len(tokens)-n+1)]

# =============================================================================
# 4) 핵심 로직
# =============================================================================
def best_keywords(text: str, top_k: int = 15,
                  min_unigram: int = 2, min_bigram: int = 2,
                  pmi_floor: float = 0.2) -> List[Tuple[str, float]]:
    kiwi = Kiwi()

    # 1) 토큰화 + 불용어 컷
    raw_tokens = tokenize_kiwi(kiwi, text)
    raw_tokens = [w for w in raw_tokens if not is_noise_token(w)]

    # 2) 병합 (예외 규칙 → 화이트리스트 기반 자동 병합)
    tokens = apply_join_rules(raw_tokens)
    tokens = auto_join_by_whitelist(tokens)
    tokens = [w for w in tokens if not is_noise_token(w)]
    if not tokens:
        return []

    # 3) 유니그램/바이그램 빈도
    uni = Counter(tokens)
    bi = Counter([
        bg for bg in build_ngrams(tokens, 2)
        if not (is_noise_token(bg[0]) or is_noise_token(bg[1]))
    ])

    # 4) 최소 빈도 필터 (화이트리스트는 예외 허용)
    uni = Counter({w: c for w, c in uni.items() if c >= min_unigram or whitelist_hit(w)})
    bi = Counter({
        bg: c for bg, c in bi.items()
        if c >= min_bigram or whitelist_hit(bg[0]) or whitelist_hit(bg[1])
    })

    total_uni = sum(uni.values()) or 1
    total_bi = sum(bi.values()) or 1

    # 5) PMI 계산 (하한선 적용)
    pmi = {}
    for (w1, w2), c12 in bi.items():
        p_w1 = uni.get(w1, 0) / total_uni
        p_w2 = uni.get(w2, 0) / total_uni
        p_w1w2 = c12 / total_bi
        score = 0.0
        if p_w1 > 0 and p_w2 > 0 and p_w1w2 > 0:
            score = math.log2(p_w1w2 / (p_w1 * p_w2))
        if score >= pmi_floor:
            pmi[(w1, w2)] = score

    # 6) 스코어링
    cand_scores: dict[str, float] = defaultdict(float)

    # (a) 바이그램: 빈도 + PMI + 보너스/페널티 + 화이트리스트 우대
    for (w1, w2), c12 in bi.items():
        phrase = f"{w1} {w2}"
        allow_by_pmi = (w1, w2) in pmi
        allow_by_wl = whitelist_hit(w1) or whitelist_hit(w2) or whitelist_hit(phrase.replace(" ", ""))  # 붙인 표면형도 확인

        if not allow_by_pmi and not allow_by_wl:
            continue

        s = 0.0
        s += c12 * 1.0
        if allow_by_pmi:
            s += pmi.get((w1, w2), 0.0) * 1.3

        # 화이트리스트 보너스
        if allow_by_wl:
            s += WL_BONUS
            if FORCE_INCLUDE_WHITELIST:
                s = max(s, WL_BONUS + 0.5)

        if brand_like(w1): s += 0.2
        if brand_like(w2): s += 0.2
        if not RE_HAN.search(phrase): s -= 0.8

        cand_scores[phrase] = max(cand_scores.get(phrase, 0.0), s)

    # (b) 유니그램: 빈도 + 보너스/페널티 + 화이트리스트 우대
    for w, c in uni.items():
        s = c * 0.7
        if brand_like(w): s += 0.2
        if not RE_HAN.search(w): s -= 0.6

        if whitelist_hit(w):
            s += WL_BONUS
            if FORCE_INCLUDE_WHITELIST:
                s = max(s, WL_BONUS + 0.2)

        cand_scores[w] = max(cand_scores.get(w, 0.0), s)

    # 7) 표면형 통합 (“초코 송이” vs “초코송이”)
    fused: dict[str, Tuple[str, float]] = {}
    for k, s in cand_scores.items():
        key = canon_form(k)
        if key not in fused or s > fused[key][1]:
            fused[key] = (k, s)

    ranked = sorted(fused.values(), key=lambda x: x[1], reverse=True)

    # 8) 중복 억제 (화이트리스트는 우선 통과)
    selected: List[Tuple[str, float]] = []
    used_parts = set()
    for kw, sc in ranked:
        parts = tuple(kw.split())
        wl_hit = whitelist_hit(kw.replace(" ", "")) or any(whitelist_hit(p) for p in parts)
        if not wl_hit:
            # (한글 기준) 구성어가 전부 이미 사용되면 스킵
            if all(canon_form(p) in used_parts for p in parts if RE_HAN.search(p)):
                continue
        selected.append((kw, sc))
        for p in parts:
            used_parts.add(canon_form(p))
        if len(selected) >= top_k:
            break

    return selected

# =============================================================================
# CLI
# =============================================================================
if __name__ == "__main__":
    print("🧠 텍스트를 붙여넣고 엔터 2번으로 종료하세요.")
    print(f"📂 DICT_DIR = {DICT_DIR}")
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
