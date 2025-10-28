import Image from "next/image";

interface PolaroidProps {
  src: string;
  small?: boolean;
  medium?: boolean;
}

export default function PolaroidCard({ src, small, medium }: PolaroidProps) {
  return (
    <div
      className={`relative flex items-center justify-center bg-white border border-[#8B857C] rounded-md shadow-sm ${
        small ? "w-16 h-20" : medium ? "w-24 h-32" : "w-36 h-48"
      }`}
    >
      <div
        className={`relative w-[85%] h-[70%] overflow-hidden rounded-sm border border-[#8B857C] bg-[#FFFEF8]`}
        style={{
          marginBottom: "14%",
        }}
      >
        <Image
          src={src}
          alt="polaroid photo"
          fill
          style={{
            objectFit: "cover",
            objectPosition: "center",
          }}
        />
      </div>
    </div>
  );
}
