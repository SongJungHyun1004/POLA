"use client";

import { useSearchParams } from "next/navigation";
import { useEffect, useMemo, useState } from "react";
import PolaroidCard from "@/app/(pola)/home/components/PolaroidCard";
import PolaroidDetail from "@/app/categories/[id]/components/PolaroidDetail";
import { searchFiles, searchTags } from "@/services/fileService";
import { Star } from "lucide-react";

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
        let res;
        if (tag) {
          res = await searchTags(tag);
        } else {
          res = await searchFiles(search);
        }

        const mapped = res.map((f: any) => ({
          ...f,
          rotation: `rotate(${Math.random() * 8 - 4}deg)`,
        }));

        setFiles(mapped);
      } catch (err) {
        console.error("검색 실패:", err);
      } finally {
        setLoading(false);
      }
    }
    load();
  }, [search, tag]);

  /** 기울기 유지 */
  const rotations = useMemo(() => files.map((f) => f.rotation), [files]);

  const TEXT_PLACEHOLDER = "/images/text_placeholder.png";

  /** 파일 선택 */
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
      platform: file.platform,
      ocr_text: file.fileType?.startsWith("text") ? file.ocrText : undefined,
    });
  };

  /** favorite 업데이트 */
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
    <div className="w-full h-full flex justify-center bg-[#FFFEF8] text-[#4C3D25]">
      <div className="w-full max-w-[1200px] h-full flex gap-8 px-6 pb-6">
        {/* ------- LEFT LIST AREA ------- */}
        <div className="flex flex-col flex-1 overflow-hidden">
          <div className="mb-2 pl-4">
            <h1 className="text-4xl font-bold mb-2">
              {tag ? `#${tag} 검색 결과` : `"${search}" 검색 결과`}
            </h1>
            <p className="text-xl text-[#7A6A48] pl-1">
              {tag
                ? `“#${tag}” 로 검색된 결과입니다.`
                : `“${search}” 로 검색된 결과입니다.`}
            </p>
          </div>

          {/* LIST SCROLL AREA */}
          <div className="flex-1 overflow-y-auto pr-2 scrollbar-thin scrollbar-thumb-[#CBBF9E]/50">
            <div
              className="
                grid gap-6 p-10
                grid-cols-1
                sm:grid-cols-2
                md:grid-cols-3
                lg:grid-cols-4
                xl:grid-cols-5
                place-items-center
                overflow-x-hidden
              "
            >
              {files.map((f, i) => {
                const isText = f.fileType?.startsWith("text");
                const isSelected = selectedFile?.id === f.fileId;

                return (
                  <div
                    key={f.fileId}
                    className="w-full flex justify-center"
                    style={{
                      transform: rotations[i],
                      transition: "transform 0.2s ease",
                      transformOrigin: "center bottom",
                    }}
                  >
                    <button
                      onClick={() => handleSelectFile(f)}
                      className={`
                        relative transition-transform
                        ${isSelected ? "scale-110 z-20" : "hover:scale-[1.07]"}
                      `}
                      style={{
                        transform: `${rotations[i]} ${
                          isSelected ? "scale(1.1)" : ""
                        }`,
                        transformOrigin: "center bottom",
                      }}
                    >
                      <PolaroidCard
                        src={isText ? TEXT_PLACEHOLDER : f.imageUrl}
                        type={f.fileType}
                        ocr_text={isText ? f.ocrText : undefined}
                      />

                      {f.favorite && (
                        <Star
                          fill={f.favorite ? "#FFD700" : "transparent"}
                          stroke="#FFD700"
                          strokeWidth={2.5}
                          className="absolute top-2 right-2 drop-shadow-sm w-6 h-6 z-10"
                        />
                      )}
                    </button>
                  </div>
                );
              })}

              {loading && (
                <div className="text-center text-[#7A6A48] py-4 col-span-5">
                  검색 중입니다...
                </div>
              )}
            </div>
          </div>
        </div>

        {/* ------- RIGHT DETAIL AREA ------- */}
        <div className="w-[400px] flex-shrink-0 border-l border-[#E3DCC8] pl-6 flex flex-col items-center justify-center">
          {selectedFile && (
            <PolaroidDetail
              id={selectedFile.id}
              src={selectedFile.src}
              tags={selectedFile.tags}
              contexts={selectedFile.context}
              date={selectedFile.created_at}
              favorite={selectedFile.favorite}
              type={selectedFile.type}
              platform={selectedFile.platform}
              ocr_text={selectedFile.ocr_text}
              onFavoriteChange={handleFavoriteChange}
            />
          )}
        </div>
      </div>
    </div>
  );
}
