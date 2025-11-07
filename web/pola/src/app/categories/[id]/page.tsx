"use client";

import { useParams } from "next/navigation";
import { useState, useEffect } from "react";
import PolaroidCard from "@/app/home/components/PolaroidCard";
import PolaroidDetail from "./components/PolaroidDetail";
import { Plus, Pencil } from "lucide-react";

import {
  getCategoryInfo,
  getCategoryTags,
  getCategoryFiles,
  getFileDetail,
} from "@/services/categoryService";

export default function CategoryPage() {
  const params = useParams();

  if (typeof params.id !== "string") {
    return (
      <div className="p-10 text-center text-xl text-red-600">
        잘못된 접근입니다. (id 없음)
      </div>
    );
  }

  const id = params.id;

  const [categoryName, setCategoryName] = useState("");
  const [tags, setTags] = useState<string[]>([]);
  const [files, setFiles] = useState<any[]>([]);
  const [selectedFile, setSelectedFile] = useState<any | null>(null);
  const [rotations, setRotations] = useState<string[]>([]);

  useEffect(() => {
    async function load() {
      try {
        const [info, tagList] = await Promise.all([
          getCategoryInfo(id),
          getCategoryTags(id),
        ]);

        setCategoryName(info.categoryName ?? "");
        setTags(tagList.map((t: any) => t.tagName));

        const fileList = await getCategoryFiles(id, 0);
        setFiles(fileList);

        setRotations(
          Array.from({ length: fileList.length }, () => {
            const deg = Math.random() * 8 - 4;
            return `rotate(${deg}deg)`;
          })
        );
      } catch (e) {
        console.error(e);
        alert("카테고리 데이터를 불러오는 중 오류가 발생했습니다.");
      }
    }

    load();
  }, [id]);

  // 파일 상세 조회
  const handleSelectFile = async (file: any) => {
    setSelectedFile({
      id: file.id,
      src: file.src ?? "/images/dummy_image_1.png",
      favorite: file.favorite,
      tags: [],
      context: "",
      created_at: "",
    });

    try {
      const detail = await getFileDetail(file.id);

      const normalizedTags = (detail.tags ?? []).map(
        (t: any) => `#${t.tagName}`
      );

      setSelectedFile({
        id: detail.id,
        src: detail.src ?? file.src ?? "/images/dummy_image_1.png",
        tags: normalizedTags,
        context: detail.context ?? "",
        created_at: detail.created_at,
      });
    } catch (e) {
      console.error(e);
    }
  };

  return (
    <div className="flex h-full bg-[#FFFEF8] text-[#4C3D25] px-8 py-6 gap-8">
      {/* 좌측 */}
      <div className="flex flex-col flex-1 overflow-hidden">
        <div className="flex items-center justify-between mb-6">
          <div>
            <h1 className="text-6xl font-bold mb-2">{categoryName}</h1>

            <p className="text-2xl text-[#7A6A48]">
              {tags.length > 0 ? tags.map((t) => `#${t}`).join(" ") : ""}
            </p>
          </div>

          <div className="flex items-center gap-4">
            <button className="p-2 rounded-full hover:bg-[#EDE6D8]">
              <Plus className="w-5 h-5" />
            </button>
          </div>
        </div>

        {/* 파일 리스트 */}
        <div className="flex-1 overflow-y-auto pr-2">
          <div className="grid grid-cols-6 gap-6 overflow-visible p-6">
            {files.map((file, i) => (
              <div
                key={file.id}
                style={{
                  transform: rotations[i] ?? "rotate(0deg)",
                  transition: "transform 0.2s ease",
                  transformOrigin: "center bottom",
                }}
              >
                <button
                  onClick={() => handleSelectFile(file)}
                  className={`relative hover:scale-[1.08] transition-transform ${
                    selectedFile?.id === file.id ? "opacity-90" : "opacity-100"
                  }`}
                >
                  <PolaroidCard src={file.src || "/images/dummy_image_1.png"} />

                  {file.favorite && (
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

      {/* 상세 영역 */}
      <div className="w-2/7 border-l border-[#E3DCC8] pl-6 flex flex-col items-center justify-center">
        <PolaroidDetail
          id={selectedFile?.id}
          src={selectedFile?.src}
          tags={selectedFile?.tags ?? []}
          contexts={selectedFile?.context ?? ""}
          date={selectedFile?.created_at}
        />
      </div>
    </div>
  );
}
