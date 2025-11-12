"use client";

import { useSearchParams } from "next/navigation";
import { useState, useEffect } from "react";
import { Plus, Pencil } from "lucide-react";
import PolaroidCard from "@/app/home/components/PolaroidCard";
import PolaroidDetail from "../categories/[id]/components/PolaroidDetail";
import { searchFiles, FileResult } from "@/services/fileService";

export default function FilesPageContent() {
  const searchParams = useSearchParams();
  const search = searchParams.get("search") ?? "";

  const [files, setFiles] = useState<FileResult[]>([]);
  const [selected, setSelected] = useState<number | null>(null);
  const [rotations, setRotations] = useState<string[]>([]);
  const [loading, setLoading] = useState(false);

  /** 검색 API 호출 */
  useEffect(() => {
    async function load() {
      if (!search.trim()) return;

      setLoading(true);
      try {
        const results = await searchFiles(search);
        setFiles(results);
      } catch (err) {
        console.error("파일 검색 실패:", err);
      } finally {
        setLoading(false);
      }
    }
    load();
  }, [search]);

  useEffect(() => {
    setRotations(
      Array.from({ length: files.length }, () => {
        const deg = Math.random() * 8 - 4;
        return `rotate(${deg}deg)`;
      })
    );
  }, [files]);

  const selectedFile = files.find((f) => f.fileId === selected);

  /** favorite 상태 변경 핸들러 (Detail ↔ List 동기화) */
  const handleFavoriteChange = (newFavorite: boolean) => {
    if (!selectedFile) return;
    setFiles((prev) =>
      prev.map((f) =>
        f.fileId === selectedFile.fileId ? { ...f, favorite: newFavorite } : f
      )
    );
  };

  return (
    <div className="flex h-full bg-[#FFFEF8] text-[#4C3D25] px-8 py-6 gap-8">
      {/* 좌측 - 결과 리스트 */}
      <div className="flex flex-col flex-1 overflow-hidden">
        <div className="flex items-center justify-between mb-6">
          <div>
            <h1 className="text-6xl font-bold mb-2">Search</h1>
            <p className="text-2xl text-[#7A6A48]">
              {search ? `"${search}" 검색 결과` : "전체 파일 목록"}
            </p>
          </div>
        </div>

        {/* 파일 목록 */}
        <div className="flex-1 overflow-y-auto pr-2">
          {loading ? (
            <p className="text-center text-gray-500 mt-20">검색 중...</p>
          ) : (
            <div className="grid grid-cols-6 gap-6 overflow-visible p-6">
              {files.map((f, i) => (
                <div
                  key={f.fileId}
                  style={{
                    transform: rotations[i],
                    transition: "transform 0.2s ease",
                    transformOrigin: "center bottom",
                  }}
                  className="w-fit overflow-visible"
                >
                  <button
                    onClick={() => setSelected(f.fileId)}
                    className={`relative hover:scale-[1.08] transition-transform ${
                      selected === f.fileId ? "opacity-90" : "opacity-100"
                    }`}
                  >
                    <PolaroidCard
                      src={f.imageUrl || "/images/dummy_image_1.png"}
                      type={f.categoryName}
                      ocr_text={f.ocrText}
                    />
                    {f.favorite && (
                      <span className="absolute top-2 right-2 text-yellow-500 text-lg">
                        ★
                      </span>
                    )}
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* 우측 상세 */}
      <div className="w-2/7 flex-shrink-0 border-l border-[#E3DCC8] pl-6 flex flex-col items-center justify-center">
        {selectedFile && (
          <PolaroidDetail
            id={selectedFile.fileId}
            src={selectedFile.imageUrl}
            tags={
              selectedFile.tags
                ? selectedFile.tags
                    .split(",")
                    .map((t) =>
                      t.trim().startsWith("#") ? t.trim() : `#${t.trim()}`
                    )
                : []
            }
            contexts={selectedFile.context}
            date={selectedFile.createdAt}
            favorite={selectedFile.favorite ?? false}
            type={selectedFile.categoryName}
            ocr_text={selectedFile.ocrText}
            onFavoriteChange={handleFavoriteChange}
          />
        )}
      </div>
    </div>
  );
}
