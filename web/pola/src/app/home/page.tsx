"use client";

import CategoryBox from "./components/CategoryBox";
import Timeline from "./components/Timeline";
import CategoryRow from "./components/CategoryRow";
import TextLink from "./components/TextLink";
import Link from "next/link";
import useAuthStore from "@/store/useAuthStore";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";

export default function HomePage() {
  const [accessToken, setAccessToken] = useState<string | null>(null);
  const [categories, setCategories] = useState<any[]>([]); // 카테고리 데이터
  const [favorites, setFavorites] = useState<any[]>([]); // 즐겨찾기 데이터
  const [reminds, setReminds] = useState<any[]>([]); // 알림 데이터
  const [timeline, setTimeline] = useState<any[]>([]); // 타임라인 데이터
  const router = useRouter();
  const { setUser } = useAuthStore();

  useEffect(() => {
    const storedToken = localStorage.getItem("accessToken");
    setAccessToken(storedToken);

    if (!storedToken) {
      router.push("/");
      return;
    }

    const fetchUserData = async () => {
      const res = await fetch(
        process.env.NEXT_PUBLIC_POLA_API_BASE_URL + "/users/me",
        {
          headers: {
            Authorization: `Bearer ${storedToken}`,
          },
        }
      );

      if (res.ok) {
        const data = await res.json();
        const { display_name, profile_image_url } = data.data;

        setUser({
          id: data.data.id,
          email: data.data.email,
          display_name,
          profile_image_url,
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
            Authorization: `Bearer ${storedToken}`,
          },
        }
      );

      if (res.ok) {
        const data = await res.json();
        const { categories, favorites, reminds, timeline } = data.data;

        setCategories(categories);
        setFavorites(favorites);
        setReminds(reminds);
        setTimeline(timeline);
      } else {
        console.error("Failed to fetch home data");
      }
    };

    if (storedToken) {
      fetchUserData();
      fetchHomeData();
    }
  }, [accessToken, router, setUser]);

  return (
    <main className="h-full text-[#4C3D25] px-8 pb-12 bg-[#FFFEF8]">
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 h-full">
        {/* 좌측 영역 */}
        <div className="flex flex-col items-center justify-start gap-12 w-full mt-16">
          <div className="flex justify-center gap-10 w-full">
            <Link href={"/favorite"}>
              <CategoryBox text="Favorite" />
            </Link>
            <div className="flex w-8" />
            <Link href={"/remind"}>
              <CategoryBox text="Remind" />
            </Link>
          </div>

          <div className="flex flex-col items-center justify-center w-full mt-8">
            <Timeline timeline={timeline} /> {/* Timeline에 데이터를 전달 */}
            <TextLink text="Timeline" link="/timeline" />
          </div>
        </div>

        {/* 우측 영역 */}
        <div className="flex flex-col gap-8 overflow-y-auto pr-2 w-full h-full">
          {categories.slice(0, 5).map((category) => {
            const imgSrc = `/images/dummy_image_1.png`; // 임시 이미지 사용

            return (
              <div key={category.categoryId} className="w-full flex-shrink-0">
                <TextLink
                  text={category.categoryName}
                  link={`/categories/${category.categoryId}`}
                />
                <CategoryRow files={category.files} />
              </div>
            );
          })}
        </div>
      </div>
    </main>
  );
}
