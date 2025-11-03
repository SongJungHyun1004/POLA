import PolaroidCard from "./PolaroidCard";

interface CategoryBoxProps {
  text: string;
}

export default function CategoryBox({ text }: CategoryBoxProps) {
  return (
    <div className="flex flex-col items-center mt-8">
      {" "}
      <div className="relative bg-[#FEF5DA] border border-[#D0A773] rounded-lg shadow-md w-56 h-48 flex flex-col items-center justify-end overflow-visible">
        <div className="absolute -top-8 flex justify-center gap-2">
          <div className="rotate-[-10deg] translate-y-[6px]">
            <PolaroidCard medium src="/images/dummy_image_1.png" />
          </div>
          <div className="rotate-[3deg] z-10">
            <PolaroidCard medium src="/images/dummy_image_1.png" />
          </div>
          <div className="rotate-[8deg] translate-y-[8px]">
            <PolaroidCard medium src="/images/dummy_image_1.png" />
          </div>
        </div>

        <div className="pb-3">
          <p className="font-semibold text-[#B0804C] text-lg">{text}</p>
        </div>
      </div>
    </div>
  );
}
