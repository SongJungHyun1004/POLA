"use client";

import CategoryBox from "./components/CategoryBox";
import Timeline from "./components/Timeline";
import CategoryRow from "./components/CategoryRow";
import TextLink from "./components/TextLink";
import useAuthStore from "@/store/useAuthStore";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { getUserMe, getUserHome } from "@/services/userService";

export default function HomePage() {
  const router = useRouter();
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
      // 1) 사용자 정보
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

      // 2) 홈 데이터
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

  return (
    <main className="h-full text-[#4C3D25] px-8 pb-12 bg-[#FFFEF8]">
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 h-full">
        {/* 좌측 영역 */}
        <div className="flex flex-col items-center justify-start gap-12 w-full mt-16">
          <div className="flex justify-center gap-10 w-full">
            {/* Favorite Box */}
            <div
              className="cursor-pointer"
              onClick={() => {
                if (favorites.length === 0) {
                  alert("좋아요 표시한 컨텐츠가 없습니다");
                  return;
                }
                router.push("/favorite");
              }}
            >
              <CategoryBox text="Favorite" files={favorites} />
            </div>

            <div className="flex w-8" />

            {/* Remind Box */}
            <div
              className="cursor-pointer"
              onClick={() => {
                if (reminds.length === 0) {
                  alert("리마인드 할 컨텐츠가 없습니다");
                  return;
                }
                router.push("/remind");
              }}
            >
              <CategoryBox text="Remind" files={reminds} />
            </div>
          </div>

          <div className="flex flex-col items-center justify-center w-full mt-8">
            <Timeline timeline={timeline} />
            <TextLink text="Timeline" link="/timeline" />
          </div>
        </div>

        {/* 우측 카테고리 영역 */}
        <div className="flex flex-col gap-8 overflow-y-auto pr-2 w-full h-full">
          {categories.map((cat) => (
            <div key={cat.categoryId} className="w-full flex-shrink-0">
              <TextLink
                text={cat.categoryName}
                link={`/categories/${cat.categoryId}`}
              />

              <CategoryRow files={cat.files ?? []} />
            </div>
          ))}
        </div>
      </div>
    </main>
  );
}
