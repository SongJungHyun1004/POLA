"use client";

import Image from "next/image";
import Link from "next/link";
import { useEffect } from "react";

export default function LandingPage() {
  /**
   * Google 로그인 성공 콜백
   */
  const handleCredentialResponse = async (response: any) => {
    const idToken = response.credential;

    console.log("Google ID Token:", idToken);

    try {
      // 1) Google ID Token을 백엔드에 전송
      const res = await fetch(
        process.env.NEXT_PUBLIC_POLA_API_BASE_URL + "/oauth/token",
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ idToken }),
        }
      );

      if (!res.ok) {
        console.error("OAuth 요청 실패:", res.status);
        alert("로그인 요청 중 오류가 발생했습니다.");
        return;
      }

      const data = await res.json();
      const status = res.status; // 200 or 201
      const { accessToken, refreshToken } = data.data;

      // 2) JWT 토큰을 localStorage에 저장
      localStorage.setItem("accessToken", accessToken);
      localStorage.setItem("refreshToken", refreshToken);

      // 3) 상태 코드에 따라 이동
      if (status === 201) {
        window.location.href = "/onboarding";
      } else if (status === 200) {
        window.location.href = "/home";
      } else {
        alert("예상치 못한 응답입니다.");
      }
    } catch (error) {
      console.error("로그인 중 오류:", error);
      alert("로그인 중 문제가 발생했습니다.");
    }
  };

  /**
   * Google Login Script 초기화
   */
  useEffect(() => {
    // Google SDK 로드
    const script = document.createElement("script");

    if (process.env.NEXT_PUBLIC_GOOGLE_CLIENT_SOURCE) {
      script.src = process.env.NEXT_PUBLIC_GOOGLE_CLIENT_SOURCE;
    } else {
      console.error(
        "NEXT_PUBLIC_GOOGLE_CLIENT_SOURCE 환경 변수가 설정되지 않았습니다."
      );
    }

    script.async = true;
    script.onload = () => {
      // @ts-ignore
      window.google.accounts.id.initialize({
        client_id: process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID,
        callback: handleCredentialResponse,
        ux_mode: "popup",
        prompt_parent_id: "googleSignInDiv",
        auto_select: false,
      });

      // 로그인 버튼 렌더링
      // @ts-ignore
      window.google.accounts.id.renderButton(
        document.getElementById("googleSignInDiv"),
        {
          theme: "outline",
          size: "large",
          text: "signin_with",
        }
      );
    };
    document.body.appendChild(script);

    return () => {
      document.body.removeChild(script);
    };
  }, []);

  /**
   * 페이지 진입 시 자동 로그인 처리
   */
  useEffect(() => {
    const accessToken = localStorage.getItem("accessToken");
    const refreshToken = localStorage.getItem("refreshToken");

    if (accessToken && refreshToken) {
      // TODO: 추후 /api/v1/auth/me 같은 엔드포인트로 토큰 검증 후 자동 로그인 처리
      console.log("JWT 토큰이 존재합니다. 자동 로그인 로직 실행 예정.");
      // window.location.href = "/home"; // 실제 API 연결 시 활성화
    }
  }, []);

  return (
    <main className="flex flex-col items-center justify-center min-h-screen bg-[#FFFEF8]">
      <Image
        src="/images/POLA_logo_1.png"
        alt="Pola Logo"
        width={360}
        height={360}
        priority
      />

      <Link href="/home">
        <h1 className="text-4xl font-semibold text-[#4C3D25] mt-8 mb-6">
          나만의 스크랩북, POLA
        </h1>
      </Link>

      {/* Google Button Mount Point */}
      <div id="googleSignInDiv"></div>
    </main>
  );
}
