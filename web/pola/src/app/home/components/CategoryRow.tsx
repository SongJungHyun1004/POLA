import { useEffect, useState } from "react";
import PolaroidCard from "./PolaroidCard";

interface CategoryRowProps {
  files: any[];
}

export default function CategoryRow({ files }: CategoryRowProps) {
  const [rotations, setRotations] = useState<string[]>([]);

  useEffect(() => {
    const newRotations = Array.from({ length: files.length }, () => {
      const deg = Math.random() * 12 - 6;
      return `rotate(${deg}deg)`;
    });
    setRotations(newRotations);
  }, [files.length]);
  if (rotations.length === 0) return null;

  return (
    <div className="flex flex-col mb-4 w-full overflow-visible">
      <div className="grid grid-cols-3 sm:grid-cols-4 md:grid-cols-5 lg:grid-cols-5 gap-3 px-10 overflow-visible justify-items-center">
        {files.slice(0, 5).map((file, i) => (
          <div
            key={i}
            style={{
              transform: rotations[i],
              transition: "transform 0.2s ease",
              transformOrigin: "center bottom",
            }}
            className="w-fit"
          >
            <div className="hover:scale-[1.08] transition-transform">
              <PolaroidCard src={file.src || "/images/dummy_image_1.png"} />{" "}
              {/* file.src로 이미지 경로 전달 */}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
