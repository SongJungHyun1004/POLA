"use client";

import { useRouter, usePathname } from "next/navigation";
import { useEffect, useState } from "react";
import { getUserMe, getUserHome } from "@/services/userService";
import useAuthStore from "@/store/useAuthStore";
import { Home, Star, Bell, Clock } from "lucide-react";
import Link from "next/link";
import Timeline from "./components/Timeline";
import PolaroidCard from "./components/PolaroidCard";
import CategoryDropdown from "../components/CategoryDropdown";

export default function HomePage() {
  const router = useRouter();
  const pathname = usePathname();
  const { setUser } = useAuthStore();

  const [favorites, setFavorites] = useState<any[]>([]);
  const [reminds, setReminds] = useState<any[]>([]);
  const [categories, setCategories] = useState<any[]>([]);
  const [timeline, setTimeline] = useState<any[]>([]);

  useEffect(() => {
    const token = localStorage.getItem("accessToken");
    if (!token) {
      router.push("/");
      return;
    }

    const fetchData = async () => {
      const userRes = await getUserMe();
      if (!userRes) {
        router.push("/");
        return;
      }

      const u = userRes.data;
      setUser({
        id: u.id,
        email: u.email,
        display_name: u.display_name,
        profile_image_url: u.profile_image_url,
      });

      const homeRes = await getUserHome();
      if (!homeRes) {
        console.error("홈 데이터 조회 실패");
        return;
      }

      const d = homeRes.data;
      setFavorites(d.favorites ?? []);
      setReminds(d.reminds ?? []);
      setCategories(d.categories ?? []);
      setTimeline(d.timeline ?? []);
    };

    fetchData();
  }, [router, setUser]);

  const getLinkClassName = (path: string) => {
    const baseClasses = "flex items-center gap-4 px-4 py-3 rounded-xl transition-colors cursor-pointer";
    if (pathname === path) {
      return `${baseClasses} bg-[#FFF4E0]`;
    }
    return `${baseClasses} hover:bg-gray-50`;
  };

  return (
    <div className="flex min-h-full bg-[#FFFEF8]">
      {/* 왼쪽 사이드바 */}
      <aside className="w-80 p-8">
        {/* 네비게이션 박스 */}
        <div className="bg-white border-2 border-[#E5DCC5] rounded-3xl p-6 shadow-sm">
          {/* 홈 */}
          <Link href="/home">
            <div className={`${getLinkClassName("/home")} mb-1`}>
              <Home className="w-5 h-5 text-[#8B7355]" />
              <span className="text-lg font-semibold text-[#4C3D25]">Home</span>
            </div>
          </Link>

          <CategoryDropdown />

          {/* Favorite */}
          <div
            onClick={() => {
              if (favorites.length === 0) {
                alert("즐겨찾기 표시한 컨텐츠가 없습니다");
                return;
              }
              router.push("/favorite");
            }}
            className={`${getLinkClassName("/favorite")} mb-1`}
          >
            <Star className="w-5 h-5 text-[#8B7355]" />
            <span className="text-lg font-semibold text-[#4C3D25]">Favorite</span>
          </div>

          {/* Remind */}
          <div
            onClick={() => {
              if (reminds.length === 0) {
                alert("리마인드 할 컨텐츠가 없습니다");
                return;
              }
              router.push("/remind");
            }}
            className={`${getLinkClassName("/remind")} mb-1`}
          >
            <Bell className="w-5 h-5 text-[#8B7355]" />
            <span className="text-lg font-semibold text-[#4C3D25]">Remind</span>
          </div>

          {/* Timeline */}
          <Link href="/timeline">
            <div className={getLinkClassName("/timeline")}>
              <Clock className="w-5 h-5 text-[#8B7355]" />
              <span className="text-lg font-semibold text-[#4C3D25]">Timeline</span>
            </div>
          </Link>
        </div>

      </aside>

      {/* 메인 콘텐츠 영역 */}
      <main className="flex-1 overflow-y-auto px-12 py-8">
        {/* 필름 스트립 타임라인 */}
        {/* <Link href="/timeline" className="mb-12 flex justify-center">
          <Timeline timeline={timeline} />
        </Link> */}

        {/* Categories 제목 */}
        <h2 className="text-5xl font-bold text-[#8B7355] mb-8">
          Recent
        </h2>

        {/* 카테고리 카드 슬라이더 */}
        <div className="w-full overflow-x-hidden group py-6">
          <div className="flex whitespace-nowrap animate-scroll-x group-hover:animation-pause">
            {[...categories, ...categories].map((cat, index) => (
              <div
                key={`${cat.categoryId}-${index}`}
                onClick={() => router.push(`/categories/${cat.categoryId}`)}
                className="cursor-pointer w-72 flex-shrink-0 mx-4"
              >
                {/* 폴라로이드 카드 스택 (3장 겹침) */}
                <div className="relative w-full aspect-[3/4]">
                  {/* 배경 카드 2 */}
                  <div className="absolute inset-0 bg-white border-2 border-[#E5DCC5] rounded-lg shadow-sm transform rotate-[-4deg] translate-y-2 translate-x-1.5" />
                  
                  {/* 배경 카드 1 */}
                  <div className="absolute inset-0 bg-white border-2 border-[#E5DCC5] rounded-lg shadow-sm transform rotate-[-2deg] translate-y-1 translate-x-0.5" />
                  
                  {/* 메인 카드 */}
                  <div className="absolute inset-0 bg-white border-2 border-[#E5DCC5] rounded-lg shadow-md overflow-hidden transform transition-transform hover:scale-105">
                    {/* 이미지 영역 (상단 75%) */}
                    <div className="h-full bg-gray-50 overflow-hidden flex items-center justify-center">
                      {cat.files && cat.files.length > 0 ? (
                        <PolaroidCard
                          src={cat.files[0].src || "/images/dummy_image_1.png"}
                          type={cat.files[0].type}
                          ocr_text={cat.files[0].ocr_text}
                          large={true} // Use large size for the card
                          categoryName={cat.categoryName}
                        />
                      ) : (
                        <PolaroidCard
                          src={"/images/POLA_null.png"}
                          type={"image/png"}
                          large={true}
                          categoryName={cat.categoryName}
                        />
                      )}
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* 카테고리가 없을 때 */}
        {categories.length === 0 && (
          <div className="text-center py-20">
            <p className="text-xl text-gray-400">카테고리가 없습니다</p>
            <p className="text-sm text-gray-300 mt-2">온보딩에서 카테고리를 추가해보세요</p>
          </div>
        )}
      </main>
    </div>
  );
}
