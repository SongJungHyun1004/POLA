"use client";

import PolaroidCard from "./PolaroidCard";

interface CategoryBoxProps {
  text: string;
  files: {
    src: string;
    type?: string;
    ocr_text?: string;
  }[];
}

export default function CategoryBox({ text, files }: CategoryBoxProps) {
  const previewFiles = files.slice(0, 3);

  return (
    <div className="flex flex-col items-center mt-8">
      <div className="relative bg-[#FEF5DA] border border-[#D0A773] rounded-lg shadow-md w-56 h-48 flex flex-col items-center justify-end overflow-visible">
        {/* ✅ 파일 수만큼만 PolaroidCard 생성 */}
        <div className="absolute -top-8 flex justify-center gap-2">
          {previewFiles.map((file, idx) => {
            const rotationClass =
              idx === 0
                ? "rotate-[-10deg] translate-y-[6px]"
                : idx === 1
                ? "rotate-[3deg] z-10"
                : "rotate-[8deg] translate-y-[8px]";

            return (
              <div key={idx} className={rotationClass}>
                <PolaroidCard
                  medium
                  src={file.src || "/images/dummy_image_1.png"}
                  type={file.type}
                  ocr_text={file.ocr_text}
                />
              </div>
            );
          })}
        </div>

        <div className="pb-3">
          <p className="font-semibold text-[#B0804C] text-lg">{text}</p>
        </div>
      </div>
    </div>
  );
}
