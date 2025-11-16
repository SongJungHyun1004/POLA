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

import { Swiper, SwiperSlide } from "swiper/react";
import { Pagination, Navigation, Autoplay } from "swiper/modules";

import "swiper/css";
import "swiper/css/pagination";
import "swiper/css/navigation";

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
    const baseClasses =
      "flex items-center gap-4 px-4 py-3 rounded-xl transition-colors cursor-pointer";
    if (pathname === path) {
      return `${baseClasses} bg-[#FFF4E0]`;
    }
    return `${baseClasses} hover:bg-gray-50`;
  };

  return (
    <div className="flex min-h-full bg-[#FFFEF8] ">
      {/* 메인 콘텐츠 영역 */}
      <main className="flex-1 overflow-y-auto py-8">
        <div className="max-w-container">
          {/* 필름 스트립 타임라인 */}
          {/* <Link href="/timeline" className="mb-12 flex justify-center">
          <Timeline timeline={timeline} />
        </Link> */}

          {/* Categories 제목 */}
          <h2 className="text-5xl font-bold text-[#8B7355] mb-8">Recent</h2>

          {/* 카테고리 카드 슬라이더 */}
          <div className="w-full group py-6">
            <div className="relative">
              <Swiper
                // Swiper에 사용할 모듈 등록
                modules={[Pagination, Navigation, Autoplay]}
                // 자동 재생
                autoplay={{
                  delay: 3000,
                  disableOnInteraction: false,
                }}
                // 페이지네이션 (하단 점)
                pagination={{ clickable: true }}
                // 네비게이션 (좌우 화살표)
                // navigation={true}

                // 반응형 설정
                slidesPerView={1} // 모바일(기본)에서는 1개
                spaceBetween={7}
                breakpoints={{
                  // 화면 너비 768px 이상일 때
                  768: {
                    slidesPerView: 2,
                    spaceBetween: 10,
                  },
                  // 화면 너비 1024px 이상일 때 (요청하신 3개)
                  1024: {
                    slidesPerView: 3,
                    spaceBetween: 10,
                  },
                }}
                //  Swiper에 고유 클래스 부여 (CSS 커스텀용)
                className="pola-swiper"
              >
                {categories.map((cat, index) => (
                  <SwiperSlide
                    key={`${cat.categoryId}-${index}`}
                    onClick={() => router.push(`/categories/${cat.categoryId}`)}
                    className="cursor-pointer flex-shrink-0" // mx-6 제거 (spaceBetween이 대신함)
                  >
                    <div className="relative w-72 h-96">
                      {/* 배경 카드 2 */}
                      <div className="absolute w-72 h-96 bg-white border-2 border-[#E5DCC5] rounded-lg shadow-sm transform rotate-[-4deg] translate-y-2 translate-x-1.5" />

                      {/* 배경 카드 1 */}
                      <div className="absolute w-72 h-96 bg-white border-2 border-[#E5DCC5] rounded-lg shadow-sm transform rotate-[-2deg] translate-y-1 translate-x-0.5" />

                      {/* 메인 카드 */}
                      <div className="absolute w-72 h-96 bg-white border-2 border-[#E5DCC5] rounded-lg shadow-md overflow-hidden transform transition-transform hover:scale-105">
                        {cat.files && cat.files.length > 0 ? (
                          <PolaroidCard
                            src={
                              cat.files[0].src || "/images/dummy_image_1.png"
                            }
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
                  </SwiperSlide>
                ))}
              </Swiper>
            </div>
          </div>

          {/* 카테고리가 없을 때 */}
          {categories.length === 0 && (
            <div className="text-center py-20">
              <p className="text-xl text-gray-400">카테고리가 없습니다</p>
              <p className="text-sm text-gray-300 mt-2">
                온보딩에서 카테고리를 추가해보세요
              </p>
            </div>
          )}
        </div>
      </main>
    </div>
  );
}
