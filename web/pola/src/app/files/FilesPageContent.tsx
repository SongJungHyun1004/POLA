"use client";

import { useSearchParams } from "next/navigation";
import { useEffect, useMemo, useState } from "react";
import PolaroidCard from "@/app/home/components/PolaroidCard";
import PolaroidDetail from "@/app/categories/[id]/components/PolaroidDetail";
import { searchFiles, searchTags } from "@/services/fileService";

export default function FilesPage() {
  const params = useSearchParams();
  const search = params.get("search") ?? "";
  const tag = params.get("tag") ?? "";

  const [files, setFiles] = useState<any[]>([]);
  const [selectedFile, setSelectedFile] = useState<any | null>(null);
  const [loading, setLoading] = useState(false);

  /** 검색 실행 */
  useEffect(() => {
    async function load() {
      if (!search && !tag) return;
      setLoading(true);

      try {
        if (tag) {
          const res = await searchTags(tag);
          const mapped = res.map((f: any) => ({
            ...f,
            rotation: `rotate(${Math.random() * 8 - 4}deg)`,
          }));
          setFiles(mapped);
        } else if (search) {
          const res = await searchFiles(search);
          const mapped = res.map((f: any) => ({
            ...f,
            rotation: `rotate(${Math.random() * 8 - 4}deg)`,
          }));
          setFiles(mapped);
        }
      } catch (err) {
        console.error("검색 실패:", err);
      } finally {
        setLoading(false);
      }
    }
    load();
  }, [search, tag]);

  /** 카드 기울기 고정 (기존 CategoryPage와 동일) */
  const rotations = useMemo(() => {
    return files.map((f) => f.rotation);
  }, [files]);

  const TEXT_PLACEHOLDER = "/images/text_placeholder.png";

  /** 파일 선택 시 상세 정보 설정 */
  const handleSelectFile = (file: any) => {
    setSelectedFile({
      id: file.fileId,
      src: file.fileType?.startsWith("text") ? TEXT_PLACEHOLDER : file.imageUrl,
      tags: file.tags
        ? file.tags
            .split(",")
            .map((t: string) =>
              t.trim().startsWith("#") ? t.trim() : `#${t.trim()}`
            )
        : [],
      context: file.context,
      created_at: file.createdAt,
      favorite: file.favorite,
      type: file.fileType,
      ocr_text: file.fileType?.startsWith("text") ? file.ocrText : undefined,
    });
  };

  /** favorite 상태 동기화 */
  const handleFavoriteChange = (newState: boolean) => {
    if (!selectedFile) return;

    setSelectedFile((prev: any) => prev && { ...prev, favorite: newState });

    setFiles((prev) =>
      prev.map((f) =>
        f.fileId === selectedFile.id ? { ...f, favorite: newState } : f
      )
    );
  };

  return (
    <div className="flex h-full bg-[#FFFEF8] text-[#4C3D25] px-8 py-6 gap-8">
      {/* 좌측 리스트 */}
      <div className="flex flex-col flex-1 overflow-hidden">
        <div className="mb-6">
          <h1 className="text-4xl font-bold mb-2">
            {tag ? `#${tag} 검색 결과` : `"${search}" 검색 결과`}
          </h1>

          {/* 하단 설명문 */}
          <p className="text-xl text-[#7A6A48]">
            {tag
              ? `“#${tag}” 로 검색된 결과입니다.`
              : `“${search}” 로 검색된 결과입니다.`}
          </p>
        </div>

        <div className="flex-1 overflow-y-auto pr-2">
          <div className="grid grid-cols-6 gap-6 overflow-visible p-6">
            {files.map((f, i) => {
              const isText = f.fileType?.startsWith("text");

              return (
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
                    onClick={() => handleSelectFile(f)}
                    className={`relative hover:scale-[1.08] transition-transform ${
                      selectedFile?.id === f.fileId
                        ? "opacity-90"
                        : "opacity-100"
                    }`}
                  >
                    <PolaroidCard
                      src={isText ? TEXT_PLACEHOLDER : f.imageUrl}
                      type={f.fileType}
                      ocr_text={isText ? f.ocrText : undefined}
                    />

                    {f.favorite && (
                      <span className="absolute top-2 right-2 text-yellow-500 text-lg">
                        ★
                      </span>
                    )}
                  </button>
                </div>
              );
            })}

            {loading && (
              <div className="text-center text-[#7A6A48] py-4 col-span-6">
                검색 중입니다...
              </div>
            )}
          </div>
        </div>
      </div>

      {/* 우측 상세 */}
      <div className="w-2/7 border-l border-[#E3DCC8] pl-6 flex flex-col items-center justify-center">
        {selectedFile && (
          <PolaroidDetail
            id={selectedFile.id}
            src={selectedFile.src}
            tags={selectedFile.tags}
            contexts={selectedFile.context}
            date={selectedFile.created_at}
            favorite={selectedFile.favorite}
            type={selectedFile.type}
            ocr_text={selectedFile.ocr_text}
            onFavoriteChange={handleFavoriteChange}
          />
        )}
      </div>
    </div>
  );
}
