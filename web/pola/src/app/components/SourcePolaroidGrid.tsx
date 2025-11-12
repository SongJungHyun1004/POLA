"use client";

import PolaroidCard from "@/app/home/components/PolaroidCard";

interface SourcePolaroidGridProps {
  sources: {
    id: number;
    src: string;
    context: string;
    tags: string[];
  }[];
}

export default function SourcePolaroidGrid({
  sources,
}: SourcePolaroidGridProps) {
  if (!sources || sources.length === 0) return null;

  return (
    <div className="grid grid-cols-2 gap-4 mt-4">
      {sources.map((src) => (
        <div key={src.id} className="flex flex-col items-center">
          <PolaroidCard src={src.src} medium />
          <p className="text-xs text-[#7A6A48] mt-2 text-center leading-tight">
            {src.context.length > 80
              ? src.context.slice(0, 80) + "..."
              : src.context}
          </p>
        </div>
      ))}
    </div>
  );
}
