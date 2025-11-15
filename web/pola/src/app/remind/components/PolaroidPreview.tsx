interface Props {
  data: {
    id: number;
    src: string;
    type?: string; // image/png | text/plain
    ocr_text?: string;
    favorite?: boolean;
  };
}

export default function PolaroidPreview({ data }: Props) {
  const isText = data.type?.startsWith("text");

  return (
    <div className="relative w-[240px] h-[300px] bg-white border border-[#D8D5CC] rounded-md shadow-sm flex flex-col items-center justify-start p-3 transition-transform duration-200 hover:-translate-y-1 cursor-pointer">
      <div className="w-full h-[75%] border border-[#D8D5CC] rounded-md overflow-hidden flex items-center justify-center bg-white">
        {isText ? (
          <div
            className="w-full h-full text-[11px] leading-tight text-[#4C3D25] whitespace-pre-line break-words text-left px-2 py-2 overflow-hidden"
            style={{
              display: "-webkit-box",
              WebkitLineClamp: 12,
              WebkitBoxOrient: "vertical",
            }}
          >
            {data.ocr_text || "(텍스트 미리보기)"}
          </div>
        ) : (
          <img
            src={data.src}
            alt=""
            className="object-cover w-full h-full object-center"
          />
        )}
      </div>

      {data.favorite && (
        <span className="absolute top-2 right-2 text-yellow-500 text-xl">
          ★
        </span>
      )}
    </div>
  );
}
