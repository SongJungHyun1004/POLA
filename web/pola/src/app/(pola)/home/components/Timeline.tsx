"use client";

import { useEffect, useRef } from "react";
import Image from "next/image";

interface TimelineProps {
  timeline: any[];
}

export default function Timeline({ timeline }: TimelineProps) {
  const scrollRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const scrollContainer = scrollRef.current;
    if (!scrollContainer) return;

    let scrollPosition = 0;

    const scroll = () => {
      if (!scrollContainer) return;

      scrollPosition += 0.2;

      if (scrollPosition >= scrollContainer.scrollWidth / 2) {
        scrollPosition = 0;
      }

      scrollContainer.scrollLeft = scrollPosition;

      requestAnimationFrame(scroll);
    };

    requestAnimationFrame(scroll);
  }, []);

  /** ✅ 안전한 normalizedTimeline 생성 */
  const minCount = 10;

  const normalizedTimeline =
    timeline.length === 0
      ? Array.from({ length: minCount }, () => ({
          src: "/images/dummy_image_1.png",
        }))
      : timeline.length >= minCount
      ? timeline
      : Array.from(
          { length: minCount },
          (_, i) => timeline[i % timeline.length]
        );

  return (
    <div className="w-4/5 flex justify-center pt-8 pb-2 overflow-hidden">
      <div
        ref={scrollRef}
        className="flex overflow-hidden whitespace-nowrap max-w-full rounded-md"
      >
        {[0, 1].map((setIdx) => (
          <div key={setIdx} className="flex">
            {normalizedTimeline.map((item, i) => (
              <div
                key={i}
                className="relative w-[129px] h-40 flex-shrink-0 overflow-hidden"
              >
                <Image
                  src="/images/flim.png"
                  alt="film"
                  fill
                  className="object-cover select-none pointer-events-none"
                />

                <div className="absolute top-1/2 left-1/2 w-[120px] h-[120px] -translate-x-1/2 -translate-y-1/2 overflow-hidden rounded-md bg-[#FFFEF8] flex items-center justify-center p-2">
                  {item.type?.startsWith("text/") ? (
                    <div className="w-full h-full text-[10px] leading-tight text-[#4C3D25] whitespace-pre-line break-words overflow-hidden">
                      {item.ocr_text || "(텍스트 미리보기 없음)"}
                    </div>
                  ) : (
                    <Image
                      src={item.src}
                      alt="pola"
                      fill
                      className="object-cover object-center select-none pointer-events-none"
                    />
                  )}
                </div>
              </div>
            ))}
          </div>
        ))}
      </div>
    </div>
  );
}
