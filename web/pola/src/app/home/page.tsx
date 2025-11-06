"use client";

import CategoryBox from "./components/CategoryBox";
import Timeline from "./components/Timeline";
import CategoryRow from "./components/CategoryRow";
import TextLink from "./components/TextLink";
import useAuthStore from "@/store/useAuthStore";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";

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

    const fetchUserData = async () => {
      const res = await fetch(
        process.env.NEXT_PUBLIC_POLA_API_BASE_URL + "/users/me",
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      if (res.ok) {
        const data = await res.json();
        setUser({
          id: data.data.id,
          email: data.data.email,
          display_name: data.data.display_name,
          profile_image_url: data.data.profile_image_url,
        });
      } else {
        router.push("/");
      }
    };

    const fetchHomeData = async () => {
      const res = await fetch(
        process.env.NEXT_PUBLIC_POLA_API_BASE_URL + "/users/me/home",
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      if (res.ok) {
        const json = await res.json();
        const d = json.data;
        setFavorites(d.favorites || []);
        setReminds(d.reminds || []);
        setCategories(d.categories || []);
        setTimeline(d.timeline || []);
      } else {
        console.error("Failed to fetch home data");
      }
    };

    fetchUserData();
    fetchHomeData();
  }, [router, setUser]);

  return (
    <main className="h-full text-[#4C3D25] px-8 pb-12 bg-[#FFFEF8]">
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 h-full">
        {/* 좌측 영역 */}
        <div className="flex flex-col items-center justify-start gap-12 w-full mt-16">
          <div className="flex justify-center gap-10 w-full">
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

        {/* 우측 영역 : categories 리스트 */}
        <div className="flex flex-col gap-8 overflow-y-auto pr-2 w-full h-full">
          {categories.map((cat: any) => {
            const files = cat.files || [];

            return (
              <div key={cat.categoryId} className="w-full flex-shrink-0">
                <TextLink
                  text={cat.categoryName}
                  link={`/categories/${cat.categoryId}`}
                />

                {/* CategoryRow에 files 전달 */}
                <CategoryRow files={files} />
              </div>
            );
          })}
        </div>
      </div>
    </main>
  );
}
