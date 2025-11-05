"use client";

import { useSearchParams } from "next/navigation";
import { useState, useEffect } from "react";
import PolaroidCard from "@/app/home/components/PolaroidCard";
import { Plus, Pencil } from "lucide-react";
import PolaroidDetail from "../categories/[id]/components/PolaroidDetail";

export default function FilesPageContent() {
  const searchParams = useSearchParams();

  const search = searchParams.get("search") ?? "";
  const tags = searchParams.get("tags") ?? "";
  const category = searchParams.get("category") ?? "";

  const [selected, setSelected] = useState<number | null>(null);
  const [rotations, setRotations] = useState<string[]>([]);

  const images = Array.from({ length: 30 }, (_, i) => ({
    id: i + 1,
    src: "/images/dummy_image_1.png",
    tags: ["#태그1", "#태그2", "#태그3", "#태그4", "#태그5", "#태그6"],
    contexts: "내용을 입력하세요...",
    favorite: i % 4 === 0,
    date: "2025.10.30",
  }));

  const selectedImage = images.find((img) => img.id === selected);

  useEffect(() => {
    const newRotations = Array.from({ length: images.length }, () => {
      const deg = Math.random() * 8 - 4;
      return `rotate(${deg}deg)`;
    });
    setRotations(newRotations);
  }, [images.length]);

  const buildSearchDescription = (
    search: string,
    tags: string,
    category: string
  ) => {
    const tagPhrase = tags ? `"${tags}" 태그` : "";
    const categoryPhrase = category ? `"${category}" 카테고리` : "";
    const searchPhrase = search ? `"${search}"` : "";

    if (category && tags && search)
      return `${categoryPhrase}에서 ${tagPhrase}와 ${searchPhrase}로 검색한 결과입니다`;
    if (tags && search)
      return `${tagPhrase}와 ${searchPhrase}로 검색한 결과입니다`;
    if (category && search)
      return `${categoryPhrase}에서 ${searchPhrase}로 검색한 결과입니다`;
    if (tags && !search && !category) return `${tagPhrase}로 검색한 결과입니다`;
    if (category && !tags && !search)
      return `${categoryPhrase}에서 검색한 결과입니다`;
    if (search && !tags && !category)
      return `${searchPhrase}로 검색한 결과입니다`;
    return "전체 파일 목록입니다";
  };

  const description = buildSearchDescription(search, tags, category);

  return (
    <div className="flex h-full bg-[#FFFEF8] text-[#4C3D25] px-8 py-6 gap-8">
      {/* 좌측 */}
      <div className="flex flex-col flex-1 overflow-hidden">
        {/* 상단 */}
        <div className="flex items-center justify-between mb-6">
          <div>
            <h1 className="text-6xl font-bold mb-2">Search</h1>
            <p className="text-2xl text-[#7A6A48]">{description}</p>
          </div>

          <div className="flex items-center gap-4">
            <button className="p-2 rounded-full hover:bg-[#EDE6D8]">
              <Plus className="w-5 h-5" />
            </button>
            <button className="p-2 rounded-full hover:bg-[#EDE6D8]">
              <Pencil className="w-5 h-5" />
            </button>
          </div>
        </div>

        {/* 폴라로이드 리스트 */}
        <div className="flex-1 overflow-y-auto pr-2">
          <div className="grid grid-cols-6 gap-6 overflow-visible p-6">
            {images.map((img, i) => (
              <div
                key={img.id}
                style={{
                  transform: rotations[i],
                  transition: "transform 0.2s ease",
                  transformOrigin: "center bottom",
                }}
                className="w-fit overflow-visible"
              >
                <button
                  onClick={() => setSelected(img.id)}
                  className={`relative hover:scale-[1.08] transition-transform ${
                    selected === img.id ? "opacity-90" : "opacity-100"
                  }`}
                >
                  <PolaroidCard src={img.src} />
                  {img.favorite && (
                    <span className="absolute top-2 right-2 text-yellow-500 text-lg">
                      ★
                    </span>
                  )}
                </button>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* 우측 상세 */}
      <div className="w-2/7 flex-shrink-0 border-l border-[#E3DCC8] pl-6 flex flex-col items-center justify-center">
        <PolaroidDetail
          id={selectedImage?.id}
          src={selectedImage?.src}
          tags={selectedImage?.tags ?? []}
          contexts={selectedImage?.contexts ?? ""}
          date={selectedImage?.date}
        />
      </div>
    </div>
  );
}
