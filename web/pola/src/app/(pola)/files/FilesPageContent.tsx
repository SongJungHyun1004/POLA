"use client";

import { useSearchParams } from "next/navigation";
import { useEffect, useMemo, useState } from "react";
import PolaroidCard from "@/app/(pola)/home/components/PolaroidCard";
import {
  searchFiles,
  searchTags,
  removeFileFavorite,
  addFileFavorite,
} from "@/services/fileService";
import { Star } from "lucide-react";
import PolaroidDetail from "../categories/[id]/components/PolaroidDetail";

interface SelectedFile {
  id: number;
  src: string;
  tags: string[];
  context: string;
  created_at: string;
  favorite: boolean;
  type?: string;
  platform?: string;
  ocr_text?: string;
}

export default function FilesPage() {
  const params = useSearchParams();
  const search = params.get("search") ?? "";
  const tag = params.get("tag") ?? "";

  const [files, setFiles] = useState<any[]>([]);
  const [selectedFile, setSelectedFile] = useState<SelectedFile | null>(null);
  const [loading, setLoading] = useState(false);

  const TEXT_PLACEHOLDER = "/images/text_placeholder.png";

  /* ---------------------------------------
      선택 파일 구성 함수
  --------------------------------------- */
  const selectFile = (file: any) => {
    const mapped = {
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
    };

    setSelectedFile(mapped);
  };

  /* ---------------------------------------
      ⭐ 즐겨찾기 토글 (등록 + 해제)
  --------------------------------------- */
  const handleFavoriteToggle = async (fileId: number, nextState: boolean) => {
    try {
      if (nextState) await addFileFavorite(fileId);
      else await removeFileFavorite(fileId);

      setFiles((prev) =>
        prev.map((f) =>
          f.fileId === fileId ? { ...f, favorite: nextState } : f
        )
      );

      if (selectedFile?.id === fileId) {
        setSelectedFile((prev) => prev && { ...prev, favorite: nextState });
      }
    } catch (err) {
      console.error(err);
      alert("즐겨찾기 변경 실패");
    }
  };

  /* ---------------------------------------
      🗑 삭제
  --------------------------------------- */
  const handleFileDeleted = async (deletedId: number) => {
    const afterDelete = files.filter((f) => f.fileId !== deletedId);
    setFiles(afterDelete);

    if (selectedFile?.id === deletedId) {
      if (afterDelete.length > 0) {
        selectFile(afterDelete[0]);
      } else {
        setSelectedFile(null);
      }
    }
  };

  /* ---------------------------------------
      검색 실행
  --------------------------------------- */
  useEffect(() => {
    async function load() {
      if (!search && !tag) return;

      setLoading(true);
      setSelectedFile(null);

      try {
        const res = tag ? await searchTags(tag) : await searchFiles(search);

        const mapped = res.map((f: any) => ({
          ...f,
          rotation: `rotate(${Math.random() * 8 - 4}deg)`,
        }));

        setFiles(mapped);

        if (mapped.length > 0) {
          selectFile(mapped[0]);
        }
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    }

    load();
  }, [search, tag]);

  const rotations = useMemo(() => files.map((f) => f.rotation), [files]);

  /* ---------------------------------------
      렌더링
  --------------------------------------- */
  return (
    <div className="w-full h-full flex justify-center bg-[#FFFEF8] text-[#4C3D25]">
      <div className="w-full max-w-[1200px] h-full flex gap-8 p-6">
        {/* LEFT LIST */}
        <div className="flex flex-col flex-1 overflow-hidden">
          <div className="mb-2 pl-4">
            <h1 className="text-5xl font-bold mb-6">
              {tag ? `#${tag} 검색 결과` : `"${search}" 검색 결과`}
            </h1>
          </div>

          <div className="flex-1 overflow-y-auto pr-2 scrollbar-thin">
            {/* EMPTY UI */}
            {!loading && files.length === 0 && (
              <div className="flex flex-col items-center justify-center py-20 opacity-80">
                <img
                  src="/images/POLA_file_empty.png"
                  className="w-72 h-72 object-contain mb-6"
                />
                <p className="text-lg text-[#7A6A48]">검색된 결과가 없습니다</p>
              </div>
            )}

            {/* FILE LIST */}
            {files.length > 0 && (
              <div className="grid gap-6 p-10 grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 place-items-center">
                {files.map((f, i) => {
                  const isText = f.fileType?.startsWith("text");
                  const isSelected = selectedFile?.id === f.fileId;

                  return (
                    <div
                      key={f.fileId}
                      className="flex justify-center w-full overflow-visible relative"
                      style={{
                        transform: rotations[i],
                        transition: "transform .25s ease",
                        transformOrigin: "center bottom",

                        /* ⭐ 선택된 카드 앞으로 */
                        zIndex: isSelected ? 30 : 1,
                      }}
                    >
                      <button
                        onClick={() => selectFile(f)}
                        className={`relative transition-transform ${
                          isSelected ? "scale-110 z-20" : "hover:scale-[1.07]"
                        }`}
                        style={{
                          transform: `${rotations[i]} ${
                            isSelected ? "scale(1.1)" : ""
                          }`,
                          transformOrigin: "center bottom",
                        }}
                      >
                        {/* ⭐ 즐겨찾기 버튼 */}
                        {f.favorite && (
                          <div
                            onClick={(e) => {
                              e.stopPropagation();
                              handleFavoriteToggle(f.fileId, false);
                            }}
                            className="absolute top-2 right-2 z-20 cursor-pointer"
                          >
                            <Star
                              fill="#FFD700"
                              stroke="#FFD700"
                              strokeWidth={2.5}
                              className="w-6 h-6 drop-shadow-sm"
                            />
                          </div>
                        )}

                        <PolaroidCard
                          src={isText ? TEXT_PLACEHOLDER : f.imageUrl}
                          type={f.fileType}
                          ocr_text={isText ? f.ocrText : undefined}
                        />
                      </button>
                    </div>
                  );
                })}
              </div>
            )}

            {loading && (
              <div className="text-center text-[#7A6A48] py-4">
                검색 중입니다...
              </div>
            )}
          </div>
        </div>

        {/* RIGHT DETAIL */}
        {selectedFile && (
          <div className="w-[400px] flex-shrink-0 border-l border-[#E3DCC8] pl-6 flex flex-col items-center pt-4">
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
              onFavoriteChange={(state) =>
                handleFavoriteToggle(selectedFile.id, state)
              }
              onFileDeleted={handleFileDeleted}
            />
          </div>
        )}
      </div>
    </div>
  );
}
