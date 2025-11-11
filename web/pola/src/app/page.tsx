"use client";

import Image from "next/image";
import Link from "next/link";
import { useEffect } from "react";
import { authService } from "@/services/authService";
import { userService } from "@/services/userService";

export default function LandingPage() {
  const handleCredentialResponse = async (response: any) => {
    const idToken = response.credential;
    try {
      const res = await authService.googleLogin(idToken);
      console.log("OAuth 응답 상태:", res.status, "ok:", res.ok);

      const raw = await res.text();
      console.log("Raw 응답 본문:", raw);

      if (!res.ok) {
        console.error("OAuth 요청 실패:", res.status);
        alert("로그인 요청 중 오류가 발생했습니다.");
        return;
      }

      const data = JSON.parse(raw);
      const accessToken = data?.data?.accessToken;

      if (!accessToken) {
        console.error("Access Token 누락:", data);
        alert("Access Token을 받지 못했습니다.");
        return;
      }

      localStorage.setItem("accessToken", accessToken);

      if (res.status === 201) {
        console.log("신규 유저 → 온보딩 이동");
        window.location.href = "/onboarding";
        return;
      }

      try {
        const categories = await userService.getMyCategories();
        if (!categories || categories.length === 0) {
          console.log("카테고리 없음 → 온보딩 이동");
          window.location.href = "/onboarding";
          return;
        }
        window.location.href = "/home";
      } catch {
        window.location.href = "/onboarding";
      }
    } catch (error) {
      console.error("로그인 중 오류:", error);
      alert("로그인 중 문제가 발생했습니다.");
    }
  };

  useEffect(() => {
    const tryAutoLogin = async () => {
      const accessToken = localStorage.getItem("accessToken");
      if (!accessToken) return;

      try {
        console.log("Access Token 검증 시도 중...");
        const verify = await authService.verifyAccessToken();
        console.log("Access Token 유효:", verify);

        const categories = await userService.getMyCategories();
        if (!categories || categories.length === 0) {
          window.location.href = "/onboarding";
          return;
        }
        window.location.href = "/home";
      } catch (err) {
        console.warn("Access Token 만료 또는 무효, 재발급 시도...");

        try {
          const base = process.env.NEXT_PUBLIC_POLA_API_BASE_URL ?? "";
          const refreshRes = await fetch(`${base}/oauth/reissue`, {
            method: "POST",
            headers: {
              "X-Client-Type": "WEB",
              "Content-Type": "application/json",
            },
            credentials: "include",
          });

          if (!refreshRes.ok) {
            console.error("토큰 재발급 실패 → 로그인 페이지로 이동");
            localStorage.removeItem("accessToken");
            window.location.href = "/";
            return;
          }

          const tokenJson = await refreshRes.json();
          const newAccess = tokenJson?.data?.accessToken;
          if (!newAccess) throw new Error("새 Access Token 누락");

          localStorage.setItem("accessToken", newAccess);

          console.log("토큰 재발급 성공 → 홈 이동");
          window.location.href = "/home";
        } catch (refreshErr) {
          console.error("자동 로그인 실패:", refreshErr);
          localStorage.removeItem("accessToken");
          window.location.href = "/";
        }
      }
    };

    tryAutoLogin();
  }, []);

  useEffect(() => {
    const script = document.createElement("script");
    script.src = process.env.NEXT_PUBLIC_GOOGLE_CLIENT_SOURCE ?? "";
    script.async = true;

    script.onload = () => {
      // @ts-ignore
      window.google.accounts.id.initialize({
        client_id: process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID,
        callback: handleCredentialResponse,
        ux_mode: "popup",
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

      <div id="googleSignInDiv"></div>
    </main>
  );
}
