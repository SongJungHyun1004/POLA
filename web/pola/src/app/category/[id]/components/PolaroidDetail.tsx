"use client";

import Image from "next/image";
import { useState } from "react";
import ImageModal from "./ImageModal";
import EditModal from "./EditModal";
import ShareModal from "./ShareModal";
import { RotateCcw, Download, Share2, Pencil } from "lucide-react";

interface PolaroidDetailProps {
  id?: number;
  src?: string;
  tags: string[];
  date?: string;
  contexts: string;
  sharedView?: boolean;
  username?: string;
}

export default function PolaroidDetail({
  id,
  src,
  tags,
  date,
  contexts,
  sharedView,
  username = "username",
}: PolaroidDetailProps) {
  const [open, setOpen] = useState(false);
  const [flipped, setFlipped] = useState(false);
  const [editOpen, setEditOpen] = useState(false);
  const [context, setContext] = useState(contexts);
  const [tagState, setTagState] = useState(tags);
  const [shareOpen, setShareOpen] = useState(false);

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
        {/* Front */}
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
              className="object-cover object-center"
            />
          </div>
        </div>

        {/* Back */}
        <div className="absolute w-full h-full rotate-y-180 backface-hidden p-4 flex flex-col">
          <div className="flex justify-between items-center mb-3">
            <h2 className="text-lg font-semibold text-[#4C3D25]">Context</h2>
            <div className="flex gap-3">
              <button
                onClick={() => setEditOpen(true)}
                className={`${sharedView && "hidden"}`}
              >
                <Pencil className="w-5 h-5 text-[#4C3D25] hover:text-black" />
              </button>

              <button>
                <Download className="w-5 h-5 text-[#4C3D25] hover:text-black" />
              </button>

              <button
                onClick={() => id && setShareOpen(true)}
                className={`${sharedView && "hidden"}`}
              >
                <Share2 className="w-5 h-5 text-[#4C3D25] hover:text-black" />
              </button>
            </div>
          </div>

          {/* 읽기 전용 영역 */}
          <textarea
            className="flex-1 resize-none p-3 rounded-md text-sm text-[#4C3D25] focus:outline-none cursor-default"
            placeholder="이미지 설명"
            value="내용을 입력하세요..."
            readOnly
          />
        </div>
      </div>

      {/* Tags / Date / Flip */}
      <div className="mt-4 text-center text-[#4C3D25] flex flex-col items-center">
        <p className="text-lg">{tags?.join(" ")}</p>
        <p className="text-2xl font-semibold mt-1">{date}</p>
        <button
          className="mt-3 bg-white border border-[#8B857C] rounded-full p-2 shadow hover:bg-[#F6F1E7] transition-transform hover:rotate-180"
          onClick={() => setFlipped((prev) => !prev)}
        >
          <RotateCcw className="w-5 h-5 text-[#4C3D25]" />
        </button>
      </div>

      {/* 모달들 */}
      {open && <ImageModal src={src} onClose={() => setOpen(false)} />}
      {shareOpen && id != null && (
        <ShareModal
          id={id}
          username={username}
          onClose={() => setShareOpen(false)}
        />
      )}
      {editOpen && (
        <EditModal
          defaultTags={tagState}
          defaultContext={context}
          onClose={() => setEditOpen(false)}
          onSave={(newTags, newContext) => {
            setTagState(newTags);
            setContext(newContext);
          }}
        />
      )}
    </div>
  );
}
