package com.jinjinjara.pola.presentation.ui.screen.my

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsOfServiceScreen(
    onBackClick: () -> Unit = {}
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            TopAppBar(
                title = { Text("이용약관") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                },
                windowInsets = WindowInsets(0.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            TermsSection(
                title = "제1조 (목적)",
                content = """
                    본 약관은 진진자라(이하 "회사")가 제공하는 POLA 서비스(이하 "서비스")의 이용과 관련하여 회사와 이용자 간의 권리, 의무 및 책임사항, 기타 필요한 사항을 규정함을 목적으로 합니다.
                """.trimIndent()
            )

            TermsSection(
                title = "제2조 (정의)",
                content = """
                    1. "서비스"란 회사가 제공하는 POLA 웹사이트, 모바일 애플리케이션, 크롬 확장 프로그램을 통해 제공되는 콘텐츠 수집, 자동 분류, 재발견 서비스를 의미합니다.
                    2. "이용자"란 본 약관에 따라 회사가 제공하는 서비스를 이용하는 회원 및 비회원을 말합니다.
                    3. "회원"이란 회사와 서비스 이용계약을 체결하고 회원 ID를 부여받은 자를 말합니다.
                    4. "콘텐츠"란 이용자가 서비스를 통해 업로드하는 이미지, 텍스트, 링크 등 모든 형태의 정보를 말합니다.
                """.trimIndent()
            )

            TermsSection(
                title = "제3조 (약관의 게시와 개정)",
                content = """
                    1. 회사는 본 약관의 내용을 이용자가 쉽게 알 수 있도록 서비스 초기 화면 또는 연결화면에 게시합니다.
                    2. 회사는 필요한 경우 관련 법령을 위배하지 않는 범위에서 본 약관을 개정할 수 있습니다.
                    3. 회사가 약관을 개정할 경우에는 적용일자 및 개정사유를 명시하여 서비스 내 공지사항을 통해 최소 7일 전부터 공지합니다.
                """.trimIndent()
            )

            TermsSection(
                title = "제4조 (서비스의 제공 및 변경)",
                content = """
                    1. 회사는 다음과 같은 서비스를 제공합니다:
                       - 크롬 확장 프로그램, 웹, 모바일 앱을 통한 콘텐츠 수집
                       - AI 기반 자동 카테고리 분류 및 태그 추출
                       - 검색 및 RAG 기반 AI 도우미(포아) 상담 서비스
                       - 리마인드 및 위젯 기능
                       - 콘텐츠 공유 및 타임라인 기능
                       - 사용자 취향 분석(마이 타입) 기능
                    2. 회사는 운영상, 기술상의 필요에 따라 제공하고 있는 서비스를 변경할 수 있습니다.
                    3. 서비스의 내용, 이용방법, 이용시간에 대하여 변경이 있는 경우에는 변경사유, 변경될 서비스의 내용 및 제공일자 등을 사전에 공지합니다.
                """.trimIndent()
            )

            TermsSection(
                title = "제5조 (서비스의 중단)",
                content = """
                    1. 회사는 컴퓨터 등 정보통신설비의 보수점검, 교체 및 고장, 통신두절 또는 운영상 상당한 이유가 있는 경우 서비스의 제공을 일시적으로 중단할 수 있습니다.
                    2. 회사는 서비스 제공이 일시적으로 중단됨으로 인하여 이용자 또는 제3자가 입은 손해에 대하여 배상하지 않습니다. 단, 회사에 고의 또는 중과실이 있는 경우에는 그러하지 아니합니다.
                """.trimIndent()
            )

            TermsSection(
                title = "제6조 (회원가입)",
                content = """
                    1. 이용자는 회사가 정한 가입 양식에 따라 회원정보를 기입한 후 본 약관에 동의한다는 의사표시를 함으로써 회원가입을 신청합니다.
                    2. 회사는 제1항과 같이 회원으로 가입할 것을 신청한 이용자 중 다음 각 호에 해당하지 않는 한 회원으로 등록합니다:
                       - 타인의 명의를 이용하여 신청한 경우
                       - 회원가입 신청서의 내용을 허위로 기재한 경우
                       - 사회의 안녕과 질서 또는 미풍양속을 저해할 목적으로 신청한 경우
                """.trimIndent()
            )

            TermsSection(
                title = "제7조 (회원 탈퇴 및 자격 상실)",
                content = """
                    1. 회원은 언제든지 탈퇴를 요청할 수 있으며, 회사는 즉시 회원 탈퇴를 처리합니다.
                    2. 회원 탈퇴 시 회원이 업로드한 모든 콘텐츠는 삭제되며, 삭제된 데이터는 복구할 수 없습니다.
                    3. 회원이 다음 각 호의 사유에 해당하는 경우, 회사는 회원자격을 제한 및 정지시킬 수 있습니다:
                       - 가입 신청 시 허위 내용을 등록한 경우
                       - 다른 사람의 서비스 이용을 방해하거나 그 정보를 도용하는 등 전자상거래 질서를 위협하는 경우
                       - 서비스를 이용하여 법령 또는 본 약관이 금지하는 행위를 하는 경우
                """.trimIndent()
            )

            TermsSection(
                title = "제8조 (콘텐츠의 저장 및 관리)",
                content = """
                    1. 회원이 업로드한 콘텐츠는 AWS S3 클라우드 스토리지에 암호화되어 저장됩니다.
                    2. 회사는 회원이 업로드한 콘텐츠에 대해 다음의 처리를 수행합니다:
                       - AI(Gemini) 기반 자동 태그 추출 및 요약
                       - Vision OCR을 통한 이미지 내 텍스트 추출
                       - 자동 카테고리 분류를 위한 임베딩 생성
                       - 검색 최적화를 위한 인덱싱
                    3. 회사는 콘텐츠 분석 과정에서 추출된 정보를 서비스 개선 목적으로만 사용하며, 제3자에게 제공하지 않습니다.
                    4. 회사는 회원이 업로드한 콘텐츠의 백업 및 복구에 대한 책임을 지지 않습니다.
                """.trimIndent()
            )

            TermsSection(
                title = "제9조 (콘텐츠의 저작권)",
                content = """
                    1. 회원이 서비스 내에 게시한 콘텐츠의 저작권은 해당 회원에게 귀속됩니다.
                    2. 회원은 자신이 업로드하는 콘텐츠에 대한 적법한 권리를 보유해야 하며, 타인의 저작권을 침해하지 않을 책임이 있습니다.
                    3. 회사는 회원이 업로드한 콘텐츠가 타인의 저작권을 침해하여 발생하는 문제에 대해 책임을 지지 않습니다.
                    4. 회원이 서비스를 통해 생성한 공유 링크로 공개된 콘텐츠는 링크를 아는 사람에게 열람될 수 있습니다.
                """.trimIndent()
            )

            TermsSection(
                title = "제10조 (개인정보보호)",
                content = """
                    1. 회사는 이용자의 개인정보를 보호하기 위하여 정보통신망 이용촉진 및 정보보호 등에 관한 법률, 개인정보보호법 등 관련 법령을 준수합니다.
                    2. 회사의 개인정보 처리방침은 서비스 내 별도 페이지를 통해 공지합니다.
                    3. 회사는 서비스 제공을 위해 필요한 최소한의 개인정보만을 수집합니다.
                """.trimIndent()
            )

            TermsSection(
                title = "제11조 (AI 서비스 이용)",
                content = """
                    1. 회사는 AI 도우미(포아)를 통해 RAG 기반 검색 증강 서비스를 제공합니다.
                    2. AI 서비스는 회원이 업로드한 콘텐츠를 기반으로 응답을 생성하며, 응답의 정확성을 보장하지 않습니다.
                    3. 회원은 AI 서비스의 응답을 참고용으로만 활용해야 하며, 중요한 의사결정은 직접 확인 후 진행해야 합니다.
                    4. 회사는 할루시네이션 방지를 위한 기술적 조치를 취하고 있으나, AI 특성상 부정확한 정보가 생성될 수 있습니다.
                """.trimIndent()
            )

            TermsSection(
                title = "제12조 (회원의 의무)",
                content = """
                    회원은 다음 행위를 하여서는 안 됩니다:
                    1. 신청 또는 변경 시 허위 내용의 등록
                    2. 타인의 정보 도용
                    3. 회사가 게시한 정보의 변경
                    4. 회사가 정한 정보 이외의 정보(컴퓨터 프로그램 등) 등의 송신 또는 게시
                    5. 회사 기타 제3자의 저작권 등 지적재산권에 대한 침해
                    6. 회사 기타 제3자의 명예를 손상시키거나 업무를 방해하는 행위
                    7. 외설 또는 폭력적인 메시지, 화상, 음성, 기타 공서양속에 반하는 정보를 서비스에 공개 또는 게시하는 행위
                    8. 서비스를 영리 목적으로 이용하는 행위
                    9. 대량의 정보를 전송하여 서비스의 안정적 운영을 방해하는 행위
                """.trimIndent()
            )

            TermsSection(
                title = "제13조 (면책조항)",
                content = """
                    1. 회사는 천재지변 또는 이에 준하는 불가항력으로 인하여 서비스를 제공할 수 없는 경우에는 서비스 제공에 관한 책임이 면제됩니다.
                    2. 회사는 회원의 귀책사유로 인한 서비스 이용의 장애에 대하여 책임을 지지 않습니다.
                    3. 회사는 회원이 서비스를 이용하여 기대하는 수익을 상실한 것에 대하여 책임을 지지 않으며, 그 밖에 서비스를 통하여 얻은 자료로 인한 손해 등에 대하여도 책임을 지지 않습니다.
                    4. 회사는 회원이 업로드한 콘텐츠의 내용에 대한 책임을 지지 않습니다.
                    5. 회사는 회원 간 또는 회원과 제3자 간에 서비스를 매개로 발생한 분쟁에 대해 개입할 의무가 없으며, 이로 인한 손해를 배상할 책임도 없습니다.
                """.trimIndent()
            )

            TermsSection(
                title = "제14조 (분쟁의 해결)",
                content = """
                    1. 회사와 회원은 서비스와 관련하여 발생한 분쟁을 원만하게 해결하기 위하여 필요한 모든 노력을 하여야 합니다.
                    2. 제1항의 노력에도 불구하고 분쟁이 해결되지 않을 경우, 양 당사자는 민사소송법상의 관할법원에 소를 제기할 수 있습니다.
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "시행일: 2025년 11월 19일",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun TermsSection(
    title: String,
    content: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = content,
            fontSize = 14.sp,
            lineHeight = 22.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}