interface Props {
  data: {
    id: number;
    src: string;
    tags: string[];
  };
}

export default function PolaroidPreview({ data }: Props) {
  return (
    <div className="w-[240px] h-[300px] bg-white border border-[#D8D5CC] rounded-md shadow-sm flex flex-col items-center justify-start p-3 transition-transform duration-200 hover:-translate-y-1 cursor-pointer">
      {/* Inner photo frame */}
      <div className="w-full h-[75%] border border-[#D8D5CC] rounded-md overflow-hidden flex items-center justify-center bg-white">
        <img src={data.src} alt="" className="object-contain w-full h-full" />
      </div>
    </div>
  );
}
