interface Props {
  data: {
    id: number;
    src: string;
    favorite?: boolean;
  };
}

export default function PolaroidPreview({ data }: Props) {
  return (
    <div className="relative w-[240px] h-[300px] bg-white border border-[#D8D5CC] rounded-md shadow-sm flex flex-col items-center justify-start p-3 transition-transform duration-200 hover:-translate-y-1 cursor-pointer">
      <div className="w-full h-[75%] border border-[#D8D5CC] rounded-md overflow-hidden flex items-center justify-center bg-white">
        <img
          src={data.src}
          alt=""
          className="object-cover w-full h-full object-center"
        />
      </div>

      {data.favorite && (
        <span className="absolute top-2 right-2 text-yellow-500 text-xl">
          ★
        </span>
      )}
    </div>
  );
}
