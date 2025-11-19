"use client";

export default function TermsOfServicePage() {
  return (
    <div className="h-full overflow-y-auto bg-[#FFFEF8] text-[#4C3D25] flex flex-col">
      {/* 콘텐츠 영역 */}
      <div className="px-8 py-10 max-w-4xl mx-auto leading-relaxed pb-12">
        <h1 className="text-4xl font-bold mb-6">POLA 이용약관</h1>
        <p className="text-sm text-[#7A6A48] mb-10">시행일: 2025년 11월 19일</p>

        {/* 제1조 */}
        <section className="mb-10">
          <h2 className="text-2xl font-bold mb-4">제1조 (목적)</h2>
          <p>
            본 약관은 진진자라(이하 "회사")가 제공하는 POLA 서비스(이하
            "서비스")의 이용과 관련하여 회사와 이용자 간의 권리, 의무 및
            책임사항, 기타 필요한 사항을 규정함을 목적으로 합니다.
          </p>
        </section>

        <hr className="my-10 border-[#D6CFBC]" />

        {/* 제2조 */}
        <section className="mb-10">
          <h2 className="text-2xl font-bold mb-4">제2조 (정의)</h2>
          <ul className="list-disc ml-6 space-y-2">
            <li>
              <strong>서비스</strong>란 POLA 웹사이트, 모바일 앱, 크롬 확장
              프로그램 등을 통해 제공되는 콘텐츠 수집, 자동 분류, 재발견 기능을
              의미합니다.
            </li>
            <li>
              <strong>이용자</strong>란 본 약관에 따라 회사가 제공하는 서비스를
              이용하는 회원 및 비회원을 말합니다.
            </li>
            <li>
              <strong>회원</strong>이란 회사와 이용계약을 체결하고 회원 ID를
              부여받은 자를 말합니다.
            </li>
            <li>
              <strong>콘텐츠</strong>란 이용자가 서비스를 통해 업로드하는
              이미지, 텍스트, 링크 등 모든 형태의 정보를 말합니다.
            </li>
          </ul>
        </section>

        <hr className="my-10 border-[#D6CFBC]" />

        {/* 제3조 */}
        <section className="mb-10">
          <h2 className="text-2xl font-bold mb-4">
            제3조 (약관의 게시와 개정)
          </h2>
          <ul className="list-disc ml-6 space-y-2">
            <li>
              회사는 본 약관의 내용을 이용자가 쉽게 알 수 있도록 서비스 초기
              화면 또는 연결 화면에 게시합니다.
            </li>
            <li>
              회사는 관련 법령을 위배하지 않는 범위에서 본 약관을 개정할 수
              있습니다.
            </li>
            <li>
              개정되는 경우 적용일자 및 개정사유를 명시하여 서비스 내 공지사항을
              통해 적용일 최소 7일 전부터 공지합니다.
            </li>
          </ul>
        </section>

        <hr className="my-10 border-[#D6CFBC]" />

        {/* 제4조 */}
        <section className="mb-10">
          <h2 className="text-2xl font-bold mb-4">
            제4조 (서비스의 제공 및 변경)
          </h2>
          <p>회사는 다음과 같은 서비스를 제공합니다:</p>
          <ul className="list-disc ml-6 mt-4 space-y-1">
            <li>콘텐츠 수집(웹·모바일·크롬 확장 프로그램)</li>
            <li>AI 기반 자동 카테고리 분류 및 태그 추출</li>
            <li>검색 및 RAG 기반 AI 도우미(포아) 서비스</li>
            <li>리마인드 및 위젯 기능</li>
            <li>콘텐츠 공유 및 타임라인 기능</li>
            <li>사용자 취향 분석 기능</li>
          </ul>

          <p className="mt-4">
            서비스는 운영상 또는 기술상의 필요에 따라 변경될 수 있으며, 변경 시
            사전 공지합니다.
          </p>
        </section>

        <hr className="my-10 border-[#D6CFBC]" />

        {/* 제5조 */}
        <section className="mb-10">
          <h2 className="text-2xl font-bold mb-4">제5조 (서비스의 중단)</h2>
          <ul className="list-disc ml-6 space-y-2">
            <li>
              회사는 정보통신설비의 보수, 점검, 고장, 통신두절 등 상당한 이유가
              있는 경우 서비스 제공을 일시적으로 중단할 수 있습니다.
            </li>
            <li>
              서비스 중단으로 인해 이용자 또는 제3자에게 발생한 손해에 대해
              회사는 고의 또는 중과실이 없는 한 책임을 지지 않습니다.
            </li>
          </ul>
        </section>

        <hr className="my-10 border-[#D6CFBC]" />

        {/* 제6조 */}
        <section className="mb-10">
          <h2 className="text-2xl font-bold mb-4">제6조 (회원가입)</h2>
          <ul className="list-disc ml-6 space-y-2">
            <li>
              이용자는 가입 양식에 따라 정보를 입력하고 본 약관에 동의함으로써
              회원가입을 신청합니다.
            </li>
            <li>
              회사는 다음 각 호에 해당하지 않는 한 회원 등록을 승인합니다:
              <ul className="list-disc ml-6 mt-2 space-y-1">
                <li>타인의 명의를 도용한 경우</li>
                <li>허위 정보를 입력한 경우</li>
                <li>사회 질서 또는 공공의 안녕을 저해할 목적의 신청인 경우</li>
              </ul>
            </li>
          </ul>
        </section>

        <hr className="my-10 border-[#D6CFBC]" />

        {/* 제7조 */}
        <section className="mb-10">
          <h2 className="text-2xl font-bold mb-4">
            제7조 (회원 탈퇴 및 자격 상실)
          </h2>
          <ul className="list-disc ml-6 space-y-2">
            <li>
              회원은 언제든지 탈퇴를 요청할 수 있으며 회사는 즉시 처리합니다.
            </li>
            <li>
              회원 탈퇴 시 업로드한 모든 콘텐츠는 삭제되며 복구할 수 없습니다.
            </li>
            <li>
              다음 사유 발생 시 회원자격 제한 또는 정지:
              <ul className="list-disc ml-6 mt-2 space-y-1">
                <li>허위 정보 등록</li>
                <li>타인의 서비스 이용을 방해하거나 정보를 도용한 경우</li>
                <li>법령 또는 약관을 위반한 경우</li>
              </ul>
            </li>
          </ul>
        </section>

        <hr className="my-10 border-[#D6CFBC]" />

        {/* 제8조 */}
        <section className="mb-10">
          <h2 className="text-2xl font-bold mb-4">
            제8조 (콘텐츠의 저장 및 관리)
          </h2>
          <ul className="list-disc ml-6 space-y-2">
            <li>AWS S3에 암호화 저장</li>
            <li>
              회사가 수행하는 자동 처리:
              <ul className="list-disc ml-6 mt-2 space-y-1">
                <li>AI 기반 태그 추출 및 요약</li>
                <li>Vision OCR을 통한 텍스트 추출</li>
                <li>자동 카테고리 분류를 위한 임베딩 생성</li>
                <li>검색 최적화를 위한 인덱싱</li>
              </ul>
            </li>
            <li>추출된 정보는 서비스 개선 목적 외 사용되지 않음</li>
            <li>콘텐츠 백업·복구에 대한 책임은 이용자에게 있음</li>
          </ul>
        </section>

        <hr className="my-10 border-[#D6CFBC]" />

        {/* 제9조 */}
        <section className="mb-10">
          <h2 className="text-2xl font-bold mb-4">제9조 (콘텐츠의 저작권)</h2>
          <ul className="list-disc ml-6 space-y-2">
            <li>콘텐츠의 저작권은 해당 회원에게 귀속됩니다.</li>
            <li>
              회원은 업로드한 콘텐츠에 대한 적법한 권리를 보유해야 합니다.
            </li>
            <li>저작권 침해로 발생하는 문제는 회원의 책임입니다.</li>
            <li>
              공유 링크가 생성된 콘텐츠는 링크를 아는 사람에게 열람될 수
              있습니다.
            </li>
          </ul>
        </section>

        <hr className="my-10 border-[#D6CFBC]" />

        {/* 제10조 */}
        <section className="mb-10">
          <h2 className="text-2xl font-bold mb-4">제10조 (개인정보보호)</h2>
          <ul className="list-disc ml-6 space-y-2">
            <li>
              회사는 개인정보보호법 등 관련 법령을 준수하여 이용자의 개인정보를
              보호합니다.
            </li>
            <li>개인정보 처리방침은 별도 페이지를 통해 안내합니다.</li>
            <li>최소한의 개인정보만을 수집합니다.</li>
          </ul>
        </section>

        <hr className="my-10 border-[#D6CFBC]" />

        {/* 제11조 */}
        <section className="mb-10">
          <h2 className="text-2xl font-bold mb-4">제11조 (AI 서비스 이용)</h2>
          <ul className="list-disc ml-6 space-y-2">
            <li>
              AI 도우미(포아) 기능은 업로드한 콘텐츠 기반으로 응답을 생성합니다.
            </li>
            <li>AI 응답의 정확성은 보장되지 않습니다.</li>
            <li>
              중요한 의사결정은 반드시 이용자 스스로 확인 후 진행해야 합니다.
            </li>
            <li>AI 특성상 할루시네이션이 발생할 수 있습니다.</li>
          </ul>
        </section>

        <hr className="my-10 border-[#D6CFBC]" />

        {/* 제12조 */}
        <section className="mb-10">
          <h2 className="text-2xl font-bold mb-4">제12조 (회원의 의무)</h2>
          <ul className="list-disc ml-6 space-y-2">
            <li>허위 내용 등록 금지</li>
            <li>타인의 정보 도용 금지</li>
            <li>회사 게시 정보의 무단 변경 금지</li>
            <li>지적재산권 침해 금지</li>
            <li>명예 훼손 및 업무 방해 행위 금지</li>
            <li>공서양속에 반하는 정보 게시 금지</li>
            <li>영리 목적의 서비스 이용 금지</li>
            <li>대량 정보 전송 등 서비스 방해 행위 금지</li>
          </ul>
        </section>

        <hr className="my-10 border-[#D6CFBC]" />

        {/* 제13조 */}
        <section className="mb-10">
          <h2 className="text-2xl font-bold mb-4">제13조 (면책조항)</h2>
          <ul className="list-disc ml-6 space-y-2">
            <li>천재지변 등 불가항력 시 회사는 책임이 면제됩니다.</li>
            <li>회원의 귀책사유로 발생한 장애에 대해 책임지지 않습니다.</li>
            <li>
              회원의 기대 수익 상실 또는 자료 손해에 대해 책임지지 않습니다.
            </li>
            <li>
              회원이 업로드한 콘텐츠 내용에 대해 회사는 책임지지 않습니다.
            </li>
            <li>
              회원 간 또는 회원과 제3자 간 분쟁에 대해 회사는 개입하지 않으며
              손해도 책임지지 않습니다.
            </li>
          </ul>
        </section>

        <hr className="my-10 border-[#D6CFBC]" />

        {/* 제14조 */}
        <section className="mb-10">
          <h2 className="text-2xl font-bold mb-4">제14조 (분쟁 해결)</h2>
          <ul className="list-disc ml-6 space-y-2">
            <li>
              회사와 이용자는 발생한 분쟁을 원만하게 해결하기 위해 노력해야
              합니다.
            </li>
            <li>
              해결되지 않을 경우 민사소송법상의 관할법원에 소를 제기할 수
              있습니다.
            </li>
          </ul>
        </section>

        <p className="text-sm text-[#7A6A48] mt-12">
          마지막 업데이트: 2025년 11월 19일
        </p>
      </div>
    </div>
  );
}
