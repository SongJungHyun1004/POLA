"use client";

import Image from "next/image";
import { useEffect, useRef, useState } from "react";
import { getTimelineFiles } from "@/services/fileService";

/** 배열을 size 만큼씩 자르기 (세로 5칸 채우기 용도) */
function chunkArray<T>(array: T[], size: number): T[][] {
  const result = [];
  for (let i = 0; i < array.length; i += size) {
    result.push(array.slice(i, i + size));
  }
  return result;
}

export default function TimeLinePage() {
  const containerRef = useRef<HTMLDivElement>(null);
  const [timelineData, setTimelineData] = useState<
    { date: string; images: any[] }[]
  >([]);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(false);
  const [hasMore, setHasMore] = useState(true);
  const didFetch = useRef(false);

  const handleWheel = (e: React.WheelEvent<HTMLDivElement>) => {
    if (!containerRef.current) return;
    containerRef.current.scrollLeft += e.deltaY;
  };

  useEffect(() => {
    const controller = new AbortController();

    async function load() {
      if (loading || !hasMore) return;
      if (page === 0 && didFetch.current) return;
      didFetch.current = true;

      setLoading(true);

      try {
        const grouped = await getTimelineFiles(page, 50);

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
  }, [page]);

  useEffect(() => {
    const container = containerRef.current;
    if (!container || !hasMore) return;

    const handleScroll = () => {
      const { scrollLeft, scrollWidth, clientWidth } = container;
      if (scrollLeft + clientWidth >= scrollWidth - 200 && !loading) {
        setPage((p) => p + 1);
      }
    };

    container.addEventListener("scroll", handleScroll);
    return () => container.removeEventListener("scroll", handleScroll);
  }, [hasMore, loading]);

  if (timelineData.length === 0 && loading) {
    return (
      <div className="flex items-center justify-center h-full text-[#4C3D25] text-2xl">
        Loading...
      </div>
    );
  }

  return (
    <div className="relative w-full h-full text-[#4C3D25] overflow-hidden flex bg-[#FFFEF8] px-8 pb-6">
      {/* 왼쪽 세로 타이틀 */}
      <div className="flex-none flex items-end justify-center pr-6 pb-20">
        <h1 className="text-[56px] font-bold timeline-vertical">TimeLine</h1>
      </div>

      {/* 콘텐츠 영역 */}
      <div
        ref={containerRef}
        onWheel={handleWheel}
        className="flex-1 overflow-x-auto overflow-y-hidden no-scrollbar"
      >
        <div className="h-full flex items-end gap-10 pb-2">
          {timelineData.map((item, index) => {
            const columns = chunkArray(item.images, 5);
            const filmWidth = 160;
            const filmGap = 12;
            const extraWidth = (columns.length - 1) * (filmWidth + filmGap) * 2;

            return (
              <div
                key={index}
                className="flex flex-col items-center rounded-md"
              >
                {/* 필름 묶음 */}
                <div className="flex items-end gap-2 mb-4 min-h-[500px] rounded-md">
                  {columns.map((col, colIndex) => (
                    <div
                      key={colIndex}
                      className="flex flex-col-reverse rounded-md"
                    >
                      {col.map((img, i) => {
                        const isText =
                          img.type?.includes("text/plain") ||
                          img.src?.endsWith(".txt") ||
                          img.src?.includes("/text/");

                        return (
                          <div
                            key={i}
                            className="relative h-[129px] w-40 rounded-md"
                          >
                            {/* 필름 프레임 */}
                            <Image
                              src="/images/flim_rotate.png"
                              alt="film frame"
                              fill
                              className="object-contain pointer-events-none"
                            />

                            {/* 내부 콘텐츠 */}
                            <div className="absolute inset-0 flex items-center justify-center">
                              <div className="relative w-[120px] h-[120px] overflow-hidden rounded-md bg-[#FFFEF8] border border-[#CBBF9E] shadow-sm flex items-center justify-center p-1">
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
                                    sizes="120px"
                                    className="object-cover object-center"
                                  />
                                )}
                              </div>
                            </div>
                          </div>
                        );
                      })}
                    </div>
                  ))}
                </div>

                {/* 날짜 + 연결선 */}
                <div className="flex flex-col items-center gap-2 relative">
                  <div className="w-3 h-3 bg-[#4C3D25] rounded-full" />
                  <span className="text-sm font-medium whitespace-nowrap">
                    {item.date}
                  </span>

                  {index < timelineData.length - 1 && (
                    <div
                      className="absolute top-[6px] h-[2px] bg-[#4C3D25] left-1/2"
                      style={{
                        width: `calc(200% + 48px + ${extraWidth}px)`,
                        transform: `translateX(calc(6px - ${
                          extraWidth / 2
                        }px))`,
                      }}
                    />
                  )}
                </div>
              </div>
            );
          })}

          {/* 로딩 인디케이터 */}
          {loading && (
            <div className="flex items-center justify-center min-w-[200px] text-[#A89B82]">
              불러오는 중...
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
