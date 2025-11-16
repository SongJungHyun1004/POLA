"use client";

import { Home, Star, Bell, Clock } from "lucide-react";
import { usePathname, useRouter } from "next/navigation";
import Link from "next/link";

import CategoryDropdown from "../../components/CategoryDropdown";
import { useEffect, useState } from "react";
import { getUserHome } from "@/services/userService";

export default function HomeSidebar() {
  const pathname = usePathname();
  const router = useRouter();

  const [favorites, setFavorites] = useState<any[]>([]);
  const [reminds, setReminds] = useState<any[]>([]);

  // HomePage 안의 fetchData 중 일부만 가져옴
  useEffect(() => {
    const fetchData = async () => {
      const homeRes = await getUserHome();
      if (!homeRes) return;

      const d = homeRes.data;
      setFavorites(d.favorites ?? []);
      setReminds(d.reminds ?? []);
    };

    fetchData();
  }, []);

  const getLinkClassName = (path: string) => {
    const base =
      "flex items-center gap-4 px-4 py-3 rounded-xl transition-colors cursor-pointer";

    return pathname === path
      ? `${base} bg-[#FFF4E0]`
      : `${base} hover:bg-gray-50`;
  };

  return (
    <aside className="w-70 ml-10">
      <div className="bg-[#ffffff] border-[#E5DCC5] rounded-3xl p-6 shadow-lg">
        {/* Home */}
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
              alert("즐겨찾기 표시한 콘텐츠가 없습니다.");
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
            <span className="text-lg font-semibold text-[#4C3D25]">
              Timeline
            </span>
          </div>
        </Link>
      </div>
    </aside>
  );
}
