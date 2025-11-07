"use client";

import Image from "next/image";
import Link from "next/link";
import { useEffect } from "react";
import { authService } from "@/services/authService";

export default function LandingPage() {
  /**
   * Google 로그인 성공 콜백
   */
  const handleCredentialResponse = async (response: any) => {
    const idToken = response.credential;

    try {
      const res = await authService.googleLogin(idToken);

      if (!res.ok) {
        console.error("OAuth 요청 실패:", res.status);
        alert("로그인 요청 중 오류가 발생했습니다.");
        return;
      }

      const data = await res.json();
      const status = res.status;
      const { accessToken, refreshToken } = data.data;

      // JWT 저장
      localStorage.setItem("accessToken", accessToken);
      localStorage.setItem("refreshToken", refreshToken);

      // 리다이렉트
      if (status === 201) {
        window.location.href = "/onboarding";
      } else {
        window.location.href = "/home";
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
    const script = document.createElement("script");

    if (process.env.NEXT_PUBLIC_GOOGLE_CLIENT_SOURCE) {
      script.src = process.env.NEXT_PUBLIC_GOOGLE_CLIENT_SOURCE;
    } else {
      console.error("NEXT_PUBLIC_GOOGLE_CLIENT_SOURCE 환경변수가 없습니다.");
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
      if (document.body.contains(script)) {
        document.body.removeChild(script);
      }
    };
  }, []);

  /**
   * 자동 로그인 (TODO)
   */
  useEffect(() => {
    const accessToken = localStorage.getItem("accessToken");
    const refreshToken = localStorage.getItem("refreshToken");

    if (accessToken && refreshToken) {
      // TODO: 실제 /auth/me API로 검증해야 함
      console.log("JWT 존재: 자동 로그인 예정");
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

      {/* Google 로그인 버튼 mount point */}
      <div id="googleSignInDiv"></div>
    </main>
  );
}
