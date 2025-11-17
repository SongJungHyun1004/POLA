"use client";

import Image from "next/image";
import { useEffect, useRef, useState } from "react";
// 1. [수정] getTimelineFiles와 getFileDetail 함수를 import
import { getTimelineFiles, getFileDetail } from "@/services/fileService";
import PolaroidDetailModal from "./components/modals/PolaroidDetailModal";

export default function TimeLinePage() {
  const [timelineData, setTimelineData] = useState<
    { date: string; images: any[] }[]
  >([]);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(false);
  const [hasMore, setHasMore] = useState(true);
  const didFetch = useRef(false);

  // 2. 모달 상태(State)
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedItem, setSelectedItem] = useState<any>(null);

  useEffect(() => {
    const controller = new AbortController();

    async function load() {
      if (loading || !hasMore) return;
      if (page === 0 && didFetch.current) return;
      didFetch.current = true;

      setLoading(true);

      try {
        const grouped = await getTimelineFiles(page, 20);

        if (!grouped || grouped.length === 0) {
          setHasMore(false);
          return;
        }

        setTimelineData((prev) => {
          const merged = [...prev];
          for (const group of grouped) {
            const existing = merged.find((x) => x.date === group.date);
            if (existing) {
              existing.images.push(...group.images);
            } else {
              merged.push(group);
            }
          }
          return merged;
        });
      } catch (e) {
        console.error("❌ Timeline load error:", e);
      } finally {
        setLoading(false);
      }
    }

    load();
    return () => controller.abort();
  }, [page, hasMore, loading]);

  useEffect(() => {
    if (!hasMore) return;

    const handleScroll = () => {
      if (loading) return;
      const { scrollTop, scrollHeight, clientHeight } = document.documentElement;
      if (scrollTop + clientHeight >= scrollHeight - 300) {
        setPage((p) => p + 1);
      }
    };

    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, [hasMore, loading]);

  const handleItemClick = (img: any) => {
    // 1. 썸네일 데이터로 즉시 모달 열기
    setSelectedItem({
      ...img,
      tags: [], // 상세 정보는 나중에 로드
      contexts: "",
    });
    setIsModalOpen(true);

    // 2. 백그라운드에서 상세 정보(원본 URL 포함) 가져오기
    getFileDetail(img.id)
      .then((fileData) => {
        // 3. 상세 정보로 모달 내용 업데이트
        const modalProps = {
          ...fileData,
          contexts: fileData.context || "",
          tags: fileData.tags ? fileData.tags.map((t: any) => t.tagName) : [],
          date: fileData.created_at || fileData.date,
        };
        setSelectedItem(modalProps);
      })
      .catch((error) => {
        console.error("❌ Failed to fetch file detail:", error);
        alert("파일 상세 정보를 불러오는 데 실패했습니다.");
        // 에러 발생 시 모달을 닫을 수도 있음
        setIsModalOpen(false);
        setSelectedItem(null);
      });
  };

  if (timelineData.length === 0 && loading) {
    return (
      <div className="flex items-center justify-center h-full text-[#4C3D25] text-2xl">
        Loading...
      </div>
    );
  }

  return (
    <div className="flex justify-center">
      <div className="w-full bg-[#FFFEF8] text-[#4C3D25] max-w-[1200px]">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <h1 className="text-5xl font-bold mt-6 mb-4">TimeLine</h1>

          <div className="flex flex-col">
            {timelineData.map((item, index) => (
              <div key={index} className="flex flex-row gap-6">
                <div className="flex flex-col items-center w-10 flex-shrink-0">
                  <div className="w-4 h-4 bg-[#4C3D25] rounded-full mt-[10px]" />
                  <div className="w-0.5 flex-grow bg-[#CBBF9E]" />
                </div>

                <div className="flex flex-col gap-4 pt-1 pb-8 w-full">
                  {/* 날짜  */}
                  <div className="flex items-center">
                    <span className="text-3xl font-semibold">{item.date}</span>
                  </div>

                  {/* 필름 묶음  */}
                  <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-5 ">
                    {item.images.map((img, i) => {
                      const isText =
                        img.type?.includes("text/plain") ||
                        img.src?.endsWith(".txt") ||
                        img.src?.includes("/text/");

                      return (
                        <div
                          key={i}
                          className="relative aspect-[4/3] rounded-md cursor-pointer"
                          onClick={() => handleItemClick(img)}
                        >
                          {/* 필름 프레임 */}
                          <Image
                            src="/images/flim.png"
                            alt="film frame"
                            fill
                            className="object-fill pointer-events-none "
                          />

                          {/* 내부 콘텐츠 */}
                          <div className="absolute inset-0 flex items-center justify-center py-[10%] px-[4%]">
                            <div className="relative w-full h-full overflow-hidden rounded-xl bg-[#FFFEF8] border border-[#CBBF9E] shadow-sm flex items-center justify-center p-1">
                              {isText ? (
                                <div
                                  className="w-full h-full overflow-hidden text-[10px] leading-tight text-[#4C3D25]
                                        whitespace-pre-line break-words p-1 text-left"
                                >
                                  {img.ocr_text || "(텍스트 없음)"}
                                </div>
                              ) : (
                            <Image
                              src={img.src || "/images/dummy_image_1.png"}
                              alt="content"
                              fill
                              sizes="(max-width: 640px) 100vw, (max-width: 768px) 50vw, (max-width: 1024px) 33vw, 25vw"
                              className="object-cover object-center"
                              placeholder="blur"
                              blurDataURL="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII="
                            />
                              )}
                            </div>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                </div>
              </div>
            ))}
          </div>

          {/* 페이지 로딩 인디케이터 */}
          {loading && (
            <div className="flex items-center justify-center py-10 text-[#A89B82]">
              불러오는 중...
            </div>
          )}
        </div>
      </div>

      {isModalOpen && selectedItem && (
        <PolaroidDetailModal
          {...selectedItem}
          onClose={() => {
            setIsModalOpen(false);
            setSelectedItem(null); 
          }}
        />
      )}
    </div>
  );
}