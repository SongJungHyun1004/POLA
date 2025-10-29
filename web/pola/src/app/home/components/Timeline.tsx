"use client";

import Image from "next/image";
import { useEffect, useRef } from "react";

export default function Timeline() {
  const scrollRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const scrollContainer = scrollRef.current;
    if (!scrollContainer) return;

    let scrollPosition = 0;

    const scroll = () => {
      if (!scrollContainer) return;
      scrollPosition += 0.2; // ← 스크롤 속도 (0.1~1 사이로 조정 가능)

      // 무한 루프 효과
      if (scrollPosition >= scrollContainer.scrollWidth / 2) {
        scrollPosition = 0;
      }

      scrollContainer.scrollLeft = scrollPosition;
      requestAnimationFrame(scroll);
    };

    requestAnimationFrame(scroll);
  }, []);

  const filmCount = 10;

  return (
    <div className="w-4/5 flex justify-center pt-8 pb-2 overflow-hidden">
      {/* 타임라인 전체 너비 줄이기 */}
      <div
        ref={scrollRef}
        className="flex overflow-hidden whitespace-nowrap max-w-full rounded-md"
      >
        {/* 무한 루프를 위한 2세트 */}
        {[...Array(2)].map((_, setIdx) => (
          <div key={setIdx} className="flex">
            {[...Array(filmCount)].map((_, i) => (
              <div
                key={i}
                className="relative w-32 h-40 flex-shrink-0 overflow-hidden"
              >
                {/* 필름 배경 */}
                <Image
                  src="/images/flim.png"
                  alt="film"
                  fill
                  className="object-cover select-none pointer-events-none"
                />

                {/* 중앙 이미지 */}
                <div className="absolute top-1/2 left-1/2 w-[120px] h-[120px] -translate-x-1/2 -translate-y-1/2 overflow-hidden rounded-md bg-[#FFFEF8]">
                  <Image
                    src="/images/POLA_logo_1.png"
                    alt="pola"
                    fill
                    className="object-cover object-center select-none pointer-events-none"
                  />
                </div>
              </div>
            ))}
          </div>
        ))}
      </div>
    </div>
  );
}
