"use client";

import Image from "next/image";
import { useEffect, useRef, useState } from "react";
import { getTimelineFiles, getFileDetail } from "@/services/fileService";
import PolaroidDetailModal from "./components/modals/PolaroidDetailModal";

export default function TimeLinePage() {
  const [timelineData, setTimelineData] = useState<
    { date: string; images: any[] }[]
  >([]);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(false);
  const [hasMore, setHasMore] = useState(true);

  const loadedPages = useRef(new Set<number>());
  const scrollLock = useRef(false);
  const scrollRef = useRef<HTMLDivElement>(null);

  // 모달
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedItem, setSelectedItem] = useState<any>(null);

  /** 📌 페이지 로드 */
  useEffect(() => {
    async function load() {
      if (loading || !hasMore) return;
      if (loadedPages.current.has(page)) return;

      loadedPages.current.add(page);
      setLoading(true);

      try {
        const grouped = await getTimelineFiles(page, 20);

        if (!grouped || grouped.length === 0) {
          setHasMore(false);
          return;
        }

        setTimelineData((prev) => {
          const dateMap = new Map<string, any[]>();

          // 1) 기존 데이터 먼저 map에 넣기
          prev.forEach((group) => {
            dateMap.set(group.date, [...group.images]);
          });

          // 2) 새로 가져온 데이터 병합
          grouped.forEach((group) => {
            if (!dateMap.has(group.date)) {
              dateMap.set(group.date, [...group.images]);
            } else {
              const existing = dateMap.get(group.date)!;

              // 이미지 id 중복 제거
              const filtered = group.images.filter(
                (img) => !existing.some((e) => e.id === img.id)
              );

              dateMap.set(group.date, [...existing, ...filtered]);
            }
          });

          // 3) map → array 변환 (날짜 정렬 포함)
          const merged = Array.from(dateMap, ([date, images]) => ({
            date,
            images,
          }));

          // 최신 날짜가 위로 오도록 정렬
          merged.sort((a, b) => (a.date < b.date ? 1 : -1));

          return merged;
        });
      } catch (e) {
        console.error("❌ Timeline load error:", e);
      } finally {
        setLoading(false);
        scrollLock.current = false;
      }
    }

    load();
  }, [page, hasMore]);

  /** ⭐ 내부 스크롤 기반 무한 스크롤 */
  useEffect(() => {
    const el = scrollRef.current;
    if (!el) return;

    const handleScroll = () => {
      if (scrollLock.current) return;
      if (loading || !hasMore) return;

      const threshold = 300;
      if (el.scrollTop + el.clientHeight >= el.scrollHeight - threshold) {
        scrollLock.current = true;
        setPage((p) => p + 1);
      }
    };

    el.addEventListener("scroll", handleScroll);
    return () => el.removeEventListener("scroll", handleScroll);
  }, [loading, hasMore]);

  /** 📌 모달 클릭 */
  const handleItemClick = (img: any) => {
    setSelectedItem({ ...img, tags: [], contexts: "" });
    setIsModalOpen(true);

    getFileDetail(img.id)
      .then((fileData) => {
        const modalProps = {
          ...fileData,
          contexts: fileData.context || "",
          tags: fileData.tags ? fileData.tags.map((t: any) => t.tagName) : [],
          date: fileData.created_at || fileData.date,
        };
        setSelectedItem(modalProps);
      })
      .catch(() => {
        alert("파일 상세 정보 로드 실패");
        setIsModalOpen(false);
      });
  };

  return (
    <div className="flex justify-center h-full bg-[#FFFEF8]">
      <div
        ref={scrollRef}
        className="w-full h-full overflow-y-auto max-w-[1200px]"
      >
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <h1 className="text-5xl font-bold mt-6 mb-4">TimeLine</h1>

          <div className="flex flex-col">
            {timelineData.map((item, index) => (
              <div key={index} className="flex flex-row gap-6">
                {/* 타임라인 점 */}
                <div className="flex flex-col items-center w-10 flex-shrink-0">
                  <div className="w-4 h-4 bg-[#4C3D25] rounded-full mt-[10px]" />
                  <div className="w-0.5 flex-grow bg-[#CBBF9E]" />
                </div>

                {/* 날짜 + 이미지들 */}
                <div className="flex flex-col gap-4 pt-1 pb-8 w-full">
                  <div className="flex items-center">
                    <span className="text-3xl font-semibold">{item.date}</span>
                  </div>

                  <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-5">
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
                          <Image
                            src="/images/flim.png"
                            alt="film frame"
                            fill
                            className="object-fill pointer-events-none"
                          />

                          <div className="absolute inset-0 flex items-center justify-center py-[10%] px-[4%]">
                            <div className="relative w-full h-full overflow-hidden rounded-xl bg-[#FFFEF8] border border-[#CBBF9E] shadow-sm flex items-center justify-center p-1">
                              {isText ? (
                                <div className="w-full h-full overflow-hidden text-[10px] leading-tight text-[#4C3D25] whitespace-pre-line break-words p-1 text-left">
                                  {img.ocr_text || "(텍스트 없음)"}
                                </div>
                              ) : (
                                <Image
                                  src={img.src || "/images/dummy_image_1.png"}
                                  alt="content"
                                  fill
                                  className="object-cover object-center"
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
