"use client";

import Image from "next/image";
import { useState } from "react";
import ImageModal from "./ImageModal";
import { RotateCcw, Download, Share2 } from "lucide-react";

interface PolaroidDetailProps {
  src?: string;
  tags?: string[];
  date?: string;
}

export default function PolaroidDetail({
  src,
  tags,
  date,
}: PolaroidDetailProps) {
  const [open, setOpen] = useState(false);
  const [flipped, setFlipped] = useState(false);
  const [context, setContext] = useState("");

  if (!src) {
    return (
      <div className="flex flex-1 items-center justify-center text-[#A89B82]">
        이미지를 선택하세요.
      </div>
    );
  }

  return (
    <div className="flex flex-col items-center w-full">
      <div
        className={`relative bg-white border border-[#8B857C] rounded-md shadow-sm w-80 h-[420px] flex items-center justify-center transition-transform duration-500 [transform-style:preserve-3d] ${
          flipped ? "rotate-y-180" : ""
        }`}
      >
        {/* Front (Image) */}
        <div
          className="absolute w-full h-full backface-hidden flex flex-col items-center justify-center cursor-pointer"
          onClick={() => setOpen(true)}
        >
          <div
            className="relative w-[85%] h-[70%] overflow-hidden rounded-sm border border-[#8B857C] bg-[#FFFEF8]"
            style={{ marginBottom: "14%" }}
          >
            <Image
              src={src}
              alt="selected polaroid"
              fill
              style={{ objectFit: "cover", objectPosition: "center" }}
            />
          </div>
        </div>

        {/* Back (Context View) */}
        <div className="absolute w-full h-full rotate-y-180 backface-hidden p-4 flex flex-col">
          {/* Header Row */}
          <div className="flex justify-between items-center mb-3">
            <h2 className="text-lg font-semibold text-[#4C3D25]">Context</h2>
            <div className="flex gap-3">
              <button>
                <Download className="w-5 h-5 text-[#4C3D25] hover:text-black" />
              </button>
              <button>
                <Share2 className="w-5 h-5 text-[#4C3D25] hover:text-black" />
              </button>
            </div>
          </div>

          {/* Context input area */}
          <textarea
            className="flex-1 resize-none border border-[#C9C2B3] p-3 rounded-md text-sm focus:outline-none"
            placeholder="내용을 입력하세요..."
            value={context}
            onChange={(e) => setContext(e.target.value)}
          />

          {/* Date */}
          <div className="text-center mt-3 font-medium text-[#4C3D25]">
            {date}
          </div>
        </div>
      </div>

      {/* Tags / Date / Flip Button */}
      <div className="mt-4 text-center text-[#4C3D25] flex flex-col items-center">
        <p className="text-lg">{tags?.join(" ")}</p>
        <p className="text-2xl font-semibold mt-1">{date}</p>

        {/* Flip Button */}
        <button
          className="mt-3 bg-white border border-[#8B857C] rounded-full p-2 shadow hover:bg-[#F6F1E7] transition-transform hover:rotate-180"
          onClick={() => setFlipped((prev) => !prev)}
        >
          <RotateCcw className="w-5 h-5 text-[#4C3D25]" />
        </button>
      </div>

      {open && <ImageModal src={src} onClose={() => setOpen(false)} />}
    </div>
  );
}
