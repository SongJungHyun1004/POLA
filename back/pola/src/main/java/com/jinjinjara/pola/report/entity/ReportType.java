package com.jinjinjara.pola.report.entity;

import lombok.Getter;

/**
 * 사용자 수집 페르소나 타입 정의
 * 각 타입은 제목, 설명, S3 이미지 URL을 가짐
 */
@Getter
public enum ReportType {

    TAG_ONE_WELL(
            "태그한우물",
            "당신은 관심 분야가 확고한 사람이에요. 특정 카테고리에 깊이 있게 콘텐츠를 수집하는 스타일이네요!",
            "https://pola-storage-bucket.s3.ap-northeast-2.amazonaws.com/report-images/tag_one_well.png"
    ),

    SCREENSHOT_MASTER(
            "스크린샷 장인",
            "백 마디 말보다 한 장의 캡쳐가 중요하죠. 당신은 시각적 정보를 선호하는 수집가입니다!",
            "https://pola-storage-bucket.s3.ap-northeast-2.amazonaws.com/report-images/screenshot_master.png"
    ),

    OCTOPUS_COLLECTOR(
            "문어발 수집가",
            "넓고 얕게 다양한 분야를 탐험하는 당신! 호기심이 많고 다재다능한 탐험가 스타일이에요.",
            "https://pola-storage-bucket.s3.ap-northeast-2.amazonaws.com/report-images/octopus_collector.png"
    ),

    TRIPITAKA_MASTER(
            "팔만대장경 장인",
            "텍스트로 지식을 쌓아가는 당신은 진정한 지식 탐구형! 글로 세상을 이해하는 사람이네요.",
            "https://pola-storage-bucket.s3.ap-northeast-2.amazonaws.com/report-images/tripitaka_master.png"
    ),

    NIGHT_OWL(
            "밤도깨비",
            "밤이 되면 활동하는 야행성 수집가! 고요한 밤에 집중력이 최고조에 달하는 스타일이에요.",
            "https://pola-storage-bucket.s3.ap-northeast-2.amazonaws.com/report-images/night_owl.png"
    ),

    MIRACLE_MORNING_BEAR(
            "미라클 모닝곰",
            "아침 일찍 일어나 하루를 시작하는 당신! 상쾌한 아침에 가장 생산적인 수집가입니다.",
            "https://pola-storage-bucket.s3.ap-northeast-2.amazonaws.com/report-images/miracle_morning_bear.png"
    ),

    NO_TYPE(
            "타입 없음",
            "이번 리포트는 타입이 없어요. 더 열심히 수집해보세요! 다음 주에 더 멋진 페르소나를 발견할 수 있을 거예요.",
            "https://pola-storage-bucket.s3.ap-northeast-2.amazonaws.com/report-images/no_type.png"
    );

    private final String title;
    private final String description;
    private final String s3ImageUrl;

    ReportType(String title, String description, String s3ImageUrl) {
        this.title = title;
        this.description = description;
        this.s3ImageUrl = s3ImageUrl;
    }
}
