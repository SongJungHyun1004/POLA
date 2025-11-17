"use client";

export default function PrivacyPolicyPage() {
  return (
    <div className="h-screen overflow-y-auto bg-[#FFFEF8] text-[#4C3D25] flex flex-col">
      {/* 콘텐츠 영역 */}
      <div className="px-8 py-10 max-w-4xl mx-auto leading-relaxed pb-32">
        <h1 className="text-4xl font-bold mb-6">POLA 개인정보처리방침</h1>
        <p className="text-sm text-[#7A6A48] mb-10">
          최종 수정일: 2025년 11월 13일
        </p>

        <p className="mb-6">
          POLA("당사", "저희")는 사용자의 개인정보를 소중히 여기며, 개인정보
          보호법을 준수합니다. 본 개인정보처리방침은 POLA Chrome 확장 프로그램
          사용 시 수집되는 정보와 그 사용 방법을 설명합니다.
        </p>

        <hr className="my-10 border-[#D6CFBC]" />

        {/* 1. 수집하는 정보 */}
        <section className="mb-10">
          <h2 className="text-2xl font-bold mb-4">1. 수집하는 정보</h2>

          <h3 className="text-xl font-semibold mb-2">1.1 계정 정보</h3>
          <ul className="list-disc ml-6 mb-4">
            <li>Google 계정 정보 (이메일 주소, 이름, 프로필 사진)</li>
            <li>로그인 시 Google OAuth 2.0을 통해 수집됩니다.</li>
          </ul>

          <h3 className="text-xl font-semibold mb-2">1.2 사용자 생성 콘텐츠</h3>
          <ul className="list-disc ml-6 mb-4">
            <li>사용자가 캡처한 스크린샷 이미지</li>
            <li>사용자가 선택하여 저장한 텍스트</li>
            <li>업로드한 이미지 파일</li>
          </ul>

          <h3 className="text-xl font-semibold mb-2">1.3 메타데이터</h3>
          <ul className="list-disc ml-6">
            <li>캡처 또는 저장 시점의 페이지 URL</li>
            <li>캡처 또는 저장 시점의 페이지 제목</li>
            <li>타임스탬프</li>
          </ul>
        </section>

        <hr className="my-10 border-[#D6CFBC]" />

        {/* 2. 정보 수집 방법 */}
        <section className="mb-10">
          <h2 className="text-2xl font-bold mb-4">2. 정보 수집 방법</h2>

          <p className="mb-4">
            모든 정보는 사용자가 명시적으로 다음 기능을 실행할 때만 수집됩니다:
          </p>

          <ul className="list-disc ml-6 mb-4">
            <li>우클릭 메뉴에서 "영역 선택하여 캡처하기" 선택</li>
            <li>우클릭 메뉴에서 "이미지를 POLA에 저장하기" 선택</li>
            <li>우클릭 메뉴에서 "선택한 텍스트 가져오기" 선택</li>
            <li>팝업에서 파일 업로드</li>
          </ul>

          <p>사용자의 동의 없이 자동으로 정보를 수집하지 않습니다.</p>
        </section>

        <hr className="my-10 border-[#D6CFBC]" />

        {/* 3. 정보 사용 목적 */}
        <section className="mb-10">
          <h2 className="text-2xl font-bold mb-4">3. 정보 사용 목적</h2>

          <p>수집된 정보는 다음 목적으로만 사용됩니다:</p>

          <ul className="list-disc ml-6 mt-4">
            <li>사용자의 콘텐츠를 저장하고 관리</li>
            <li>AI 기반 자동 분류 및 정리 서비스 제공</li>
            <li>사용자 계정 인증 및 서비스 접근 관리</li>
            <li>서비스 개선 및 문제 해결</li>
          </ul>
        </section>

        <hr className="my-10 border-[#D6CFBC]" />

        {/* 4. 정보 보관 */}
        <section className="mb-10">
          <h2 className="text-2xl font-bold mb-4">4. 정보 보관</h2>

          <ul className="list-disc ml-6">
            <li>
              <strong>사용자 계정 정보</strong>: 계정 삭제 시까지 보관
            </li>
            <li>
              <strong>사용자 생성 콘텐츠</strong>: 사용자가 직접 삭제하기 전까지
              보관
            </li>
            <li>
              <strong>액세스 토큰</strong>: 로컬 저장소에만 저장되며, 로그아웃
              시 즉시 삭제
            </li>
          </ul>
        </section>

        <hr className="my-10 border-[#D6CFBC]" />

        {/* 5. 정보 공유 */}
        <section className="mb-10">
          <h2 className="text-2xl font-bold mb-4">5. 정보 공유</h2>

          <p>
            당사는 사용자의 개인정보를 제3자와 공유하지 않습니다. 단, 다음의
            경우 예외로 합니다:
          </p>

          <ul className="list-disc ml-6 mt-4 mb-6">
            <li>사용자의 명시적 동의가 있는 경우</li>
            <li>법률에 의해 요구되는 경우</li>
          </ul>

          <h3 className="text-xl font-semibold mb-3">제3자 서비스</h3>
          <ul className="list-disc ml-6">
            <li>
              <strong>Google OAuth</strong>: 사용자 인증에 사용 (Google
              개인정보처리방침 적용)
            </li>
            <li>
              <strong>Amazon S3</strong>: 이미지 및 파일 저장에 사용 (AWS
              개인정보처리방침 적용)
            </li>
          </ul>
        </section>

        <hr className="my-10 border-[#D6CFBC]" />

        {/* 6. 사용자의 권리 */}
        <section className="mb-10">
          <h2 className="text-2xl font-bold mb-4">6. 사용자의 권리</h2>

          <p className="mb-4">
            사용자는 언제든지 다음의 권리를 행사할 수 있습니다:
          </p>
          <ul className="list-disc ml-6 mb-4">
            <li>개인정보 열람 요청</li>
            <li>개인정보 수정 또는 삭제 요청</li>
            <li>계정 삭제 요청</li>
          </ul>

          <p>위 권리 행사를 원하시면 아래 연락처로 문의해주세요.</p>
        </section>

        <hr className="my-10 border-[#D6CFBC]" />

        {/* 7. 데이터 보안 */}
        <section className="mb-10">
          <h2 className="text-2xl font-bold mb-4">7. 데이터 보안</h2>

          <p>
            당사는 사용자의 정보를 안전하게 보호하기 위해 다음 조치를 취합니다:
          </p>

          <ul className="list-disc ml-6 mt-4">
            <li>HTTPS를 통한 암호화된 데이터 전송</li>
            <li>액세스 토큰 기반 인증</li>
            <li>AWS S3의 보안 기능 활용</li>
          </ul>
        </section>

        <hr className="my-10 border-[#D6CFBC]" />

        {/* 8. 쿠키 */}
        <section className="mb-10">
          <h2 className="text-2xl font-bold mb-4">8. 쿠키 및 추적 기술</h2>
          <p>POLA 확장 프로그램은 쿠키나 기타 추적 기술을 사용하지 않습니다.</p>
        </section>

        <hr className="my-10 border-[#D6CFBC]" />

        {/* 9. 어린이 */}
        <section className="mb-10">
          <h2 className="text-2xl font-bold mb-4">9. 어린이의 개인정보</h2>
          <p>
            당사는 만 14세 미만 어린이의 개인정보를 의도적으로 수집하지
            않습니다.
          </p>
        </section>

        <hr className="my-10 border-[#D6CFBC]" />

        {/* 10. 변경 */}
        <section className="mb-10">
          <h2 className="text-2xl font-bold mb-4">10. 개인정보처리방침 변경</h2>
          <p>
            본 개인정보처리방침은 필요 시 변경될 수 있으며, 변경 사항은 본
            페이지를 통해 공지됩니다.
          </p>
        </section>

        <hr className="my-10 border-[#D6CFBC]" />

        {/* 11. 연락처 */}
        <section className="mb-10">
          <h2 className="text-2xl font-bold mb-4">11. 연락처</h2>
          <p>개인정보 보호와 관련한 문의사항이 있으시면 아래로 연락해주세요:</p>

          <ul className="list-disc ml-6 mt-4">
            <li>
              <strong>이메일</strong>:{" "}
              <a
                className="underline text-blue-600"
                href="mailto:starforce.mozzi@gmail.com"
              >
                starforce.mozzi@gmail.com
              </a>
            </li>
            <li>
              <strong>웹사이트</strong>:{" "}
              <a
                className="underline text-blue-600"
                href="https://k13d204.p.ssafy.io"
                target="_blank"
              >
                https://k13d204.p.ssafy.io
              </a>
            </li>
          </ul>
        </section>

        <p className="text-sm text-[#7A6A48] mt-12">
          마지막 업데이트: 2025년 11월 13일
        </p>
      </div>

      {/* Footer */}
      <footer className="border-t border-[#D6CFBC] py-4 bg-[#F7F4EC] text-center text-sm text-[#4C3D25]">
        <small>
          © 2025 POLA. All rights reserved. | 버전 1.0.0 |
          <a className="underline ml-1" href="mailto:starforce.mozzi@gmail.com">
            문의하기
          </a>{" "}
          |
          <a
            className="underline ml-1"
            href="https://k13d204.p.ssafy.io/privacy-policy"
            target="_blank"
          >
            개인정보처리방침
          </a>
        </small>
      </footer>
    </div>
  );
}
