"use client";

import Image from "next/image";
import { useRef } from "react";

/** 배열을 size 만큼씩 자르기 (세로 5칸 채우기 용도) */
function chunkArray<T>(array: T[], size: number): T[][] {
  const result = [];
  for (let i = 0; i < array.length; i += size) {
    result.push(array.slice(i, i + size));
  }
  return result;
}

const timelineData = [
  { date: "2025.10.30", images: [1, 2, 3, 4] },
  { date: "2025.06.04", images: [1, 2] },
  { date: "2025.04.02", images: [1, 2, 3] },
  { date: "2025.03.31", images: [1] },
  { date: "2025.02.28", images: [1, 2, 3, 4, 5, 6, 7, 8, 9] },
  { date: "2025.01.05", images: [1, 2, 3, 4] },
  { date: "2025.01.04", images: [1, 2] },
  { date: "2025.01.02", images: [1, 2, 3] },
  { date: "2024.12.31", images: [1] },
  { date: "2024.12.28", images: [1, 2, 3, 4, 5] },
];

export default function TimeLinePage() {
  const containerRef = useRef<HTMLDivElement>(null);

  const handleWheel = (e: React.WheelEvent<HTMLDivElement>) => {
    if (!containerRef.current) return;
    containerRef.current.scrollLeft += e.deltaY;
  };

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

            // 필름 열에 따른 추가 길이 계산
            const filmWidth = 160; // w-40
            const filmGap = 12; // gap-3
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
                      {col.map((img, i) => (
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

                          {/* 내부 이미지 */}
                          <div className="absolute inset-0 flex items-center justify-center">
                            <div className="w-[120px] h-[120px] overflow-hidden rounded-md bg-gray-200 shadow-sm">
                              <Image
                                src="/images/dummy_image_1.png"
                                alt="content"
                                width={120}
                                height={120}
                                className="object-cover"
                              />
                            </div>
                          </div>
                        </div>
                      ))}
                    </div>
                  ))}
                </div>

                <div
                  key={index}
                  className="flex flex-col items-center gap-2 relative"
                >
                  {/* 점 */}
                  <div className="w-3 h-3 bg-[#4C3D25] rounded-full" />
                  <span className="text-sm font-medium whitespace-nowrap">
                    {item.date}
                  </span>

                  {/* 간선 */}
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
        </div>
      </div>
    </div>
  );
}
