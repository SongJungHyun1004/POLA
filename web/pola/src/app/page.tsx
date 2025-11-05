"use client";

import Image from "next/image";
import Link from "next/link";
import { useEffect } from "react";

export default function LandingPage() {
  const handleCredentialResponse = async (response: any) => {
    const idToken = response.credential;

    // 1) idToken을 백엔드로 전송
    const res = await fetch("/api/auth/google", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ idToken }),
    });

    if (res.ok) {
      window.location.href = "/home";
    } else {
      alert("로그인 실패");
    }
  };

  useEffect(() => {
    // Google script 추가
    const script = document.createElement("script");
    script.src = "https://accounts.google.com/gsi/client";
    script.async = true;
    script.onload = () => {
      // Google 로그인 초기화
      // @ts-ignore
      window.google.accounts.id.initialize({
        client_id: process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID,
        callback: handleCredentialResponse,
        ux_mode: "popup",
        prompt_parent_id: "googleSignInDiv",
        auto_select: false, // 자동 로그인 방지
      });

      // 버튼 렌더링
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
