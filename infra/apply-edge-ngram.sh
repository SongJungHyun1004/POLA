#!/bin/bash

# OpenSearch Edge N-gram 매핑 적용 스크립트
# 사용법: ./apply-edge-ngram.sh

set -e

echo "=========================================="
echo "OpenSearch Edge N-gram 매핑 적용 스크립트"
echo "=========================================="
echo ""

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# OpenSearch Pod 이름
POD_NAME="opensearch-0"
NAMESPACE="opensearch"

echo "📊 1단계: 현재 OpenSearch 상태 확인"
echo "----------------------------------------"
kubectl get pods -n $NAMESPACE

echo ""
echo "📊 2단계: 현재 인덱스 문서 수 확인"
echo "----------------------------------------"
DOC_COUNT=$(kubectl exec -n $NAMESPACE $POD_NAME -- curl -s -X GET "http://localhost:9200/files/_count" 2>/dev/null | grep -o '"count":[0-9]*' | cut -d':' -f2)
echo "현재 문서 수: $DOC_COUNT 개"

echo ""
echo -e "${YELLOW}⚠️  경고: 다음 단계에서 기존 인덱스가 삭제됩니다!${NC}"
echo -e "${YELLOW}   데이터 백업을 먼저 진행하는 것을 권장합니다.${NC}"
echo ""
read -p "계속 진행하시겠습니까? (yes/no): " CONFIRM

if [ "$CONFIRM" != "yes" ]; then
    echo "작업이 취소되었습니다."
    exit 0
fi

echo ""
echo "💾 3단계: 데이터 백업 (선택)"
echo "----------------------------------------"
read -p "데이터를 백업하시겠습니까? (yes/no): " BACKUP

if [ "$BACKUP" == "yes" ]; then
    echo "백업 중..."
    kubectl exec -n $NAMESPACE $POD_NAME -- curl -s -X GET "http://localhost:9200/files/_search?scroll=5m&size=1000&pretty" > /tmp/opensearch_backup_$(date +%Y%m%d_%H%M%S).json 2>/dev/null
    echo -e "${GREEN}✅ 백업 완료: /tmp/opensearch_backup_*.json${NC}"
fi

echo ""
echo "🗑️  4단계: 기존 인덱스 삭제"
echo "----------------------------------------"
kubectl exec -n $NAMESPACE $POD_NAME -- curl -s -X DELETE "http://localhost:9200/files" 2>/dev/null
echo -e "${GREEN}✅ 인덱스 삭제 완료${NC}"

echo ""
echo "🔧 5단계: Edge N-gram 매핑으로 인덱스 재생성"
echo "----------------------------------------"

# 매핑 JSON 파일을 Base64로 인코딩하여 전달
MAPPING_JSON=$(cat <<'EOF'
{
  "settings": {
    "number_of_shards": 1,
    "number_of_replicas": 1,
    "analysis": {
      "tokenizer": {
        "nori_mixed": {
          "type": "nori_tokenizer",
          "decompound_mode": "mixed"
        },
        "edge_ngram_tokenizer": {
          "type": "edge_ngram",
          "min_gram": 1,
          "max_gram": 10,
          "token_chars": ["letter", "digit"]
        }
      },
      "analyzer": {
        "nori_analyzer": {
          "type": "custom",
          "tokenizer": "nori_mixed",
          "filter": ["nori_posfilter", "lowercase"]
        },
        "edge_ngram_analyzer": {
          "type": "custom",
          "tokenizer": "edge_ngram_tokenizer",
          "filter": ["lowercase"]
        },
        "edge_ngram_search_analyzer": {
          "type": "custom",
          "tokenizer": "standard",
          "filter": ["lowercase"]
        }
      },
      "filter": {
        "nori_posfilter": {
          "type": "nori_part_of_speech",
          "stoptags": ["E", "J", "SC", "SE", "SF", "VCN", "VCP", "VX"]
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "fileId": { "type": "long" },
      "userId": { "type": "long" },
      "categoryName": { "type": "keyword" },
      "tags": {
        "type": "text",
        "analyzer": "nori_analyzer",
        "search_analyzer": "nori_analyzer",
        "fields": {
          "keyword": { "type": "keyword" },
          "edge_ngram": {
            "type": "text",
            "analyzer": "edge_ngram_analyzer",
            "search_analyzer": "edge_ngram_search_analyzer"
          }
        }
      },
      "context": {
        "type": "text",
        "analyzer": "nori_analyzer",
        "search_analyzer": "nori_analyzer",
        "fields": {
          "edge_ngram": {
            "type": "text",
            "analyzer": "edge_ngram_analyzer",
            "search_analyzer": "edge_ngram_search_analyzer"
          }
        }
      },
      "ocrText": {
        "type": "text",
        "analyzer": "nori_analyzer",
        "search_analyzer": "nori_analyzer",
        "fields": {
          "edge_ngram": {
            "type": "text",
            "analyzer": "edge_ngram_analyzer",
            "search_analyzer": "edge_ngram_search_analyzer"
          }
        }
      },
      "imageUrl": { "type": "keyword" },
      "createdAt": {
        "type": "date",
        "format": "yyyy-MM-dd'T'HH:mm:ss.SSSSSS||yyyy-MM-dd'T'HH:mm:ss||strict_date_optional_time||epoch_millis"
      }
    }
  }
}
EOF
)

kubectl exec -n $NAMESPACE $POD_NAME -- bash -c "curl -s -X PUT 'http://localhost:9200/files' -H 'Content-Type: application/json' -d '$MAPPING_JSON'" 2>/dev/null

echo -e "${GREEN}✅ 인덱스 생성 완료${NC}"

echo ""
echo "✅ 6단계: 매핑 확인"
echo "----------------------------------------"
MAPPING_CHECK=$(kubectl exec -n $NAMESPACE $POD_NAME -- curl -s -X GET "http://localhost:9200/files/_mapping?pretty" 2>/dev/null | grep "edge_ngram" | wc -l)

if [ "$MAPPING_CHECK" -gt 0 ]; then
    echo -e "${GREEN}✅ Edge N-gram 필드 확인됨 (${MAPPING_CHECK}개 필드)${NC}"
else
    echo -e "${RED}❌ Edge N-gram 필드를 찾을 수 없습니다!${NC}"
    exit 1
fi

echo ""
echo "=========================================="
echo -e "${GREEN}🎉 Edge N-gram 매핑 적용 완료!${NC}"
echo "=========================================="
echo ""
echo "📝 다음 단계:"
echo "1. 애플리케이션 코드 배포 (kubectl rollout restart)"
echo "2. 데이터 재색인 (자동 또는 수동)"
echo "3. 검색 테스트 (/api/v1/search/tag-suggestions?keyword=리)"
echo ""
echo "📚 자세한 내용은 OPENSEARCH_EDGE_NGRAM_GUIDE.md 참조"
