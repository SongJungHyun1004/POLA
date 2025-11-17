"use client";

import { X } from "lucide-react";
import Image from "next/image";
import { useEffect } from "react";

interface ImageModalProps {
  src: string;
  onClose: () => void;
}

export default function ImageModal({ src, onClose }: ImageModalProps) {
  useEffect(() => {
    const onKeyDown = (e: KeyboardEvent) => e.key === "Escape" && onClose();
    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, [onClose]);

  return (
    <div
      className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50"
      onClick={onClose}
    >
      <div
        className="relative w-[90vw] h-[90vh] max-w-[90vw] max-h-[90vh] flex items-center justify-center bg-none rounded-lg overflow-hidden"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="relative w-full h-full p-4">
          <Image
            src={src}
            alt="fullscreen"
            fill
            className="object-contain  shadow-lg"
          />
        </div>

        <button
          className="absolute top-3 right-3 bg-white/90 hover:bg-white p-2 rounded-full shadow-md flex items-center justify-center"
          onClick={onClose}
        >
          <X className="w-8 h-8 text-black" strokeWidth={2.5} />
        </button>
      </div>
    </div>
  );
}
