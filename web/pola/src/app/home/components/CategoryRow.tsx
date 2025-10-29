import PolaroidCard from "./PolaroidCard";

export default function CategoryRow({ imgSrc }: { imgSrc: string }) {
  const rotations = Array.from({ length: 6 }, () => {
    const deg = Math.random() * 12 - 6;
    return `rotate(${deg}deg)`;
  });

  return (
    <div className="flex flex-col mb-4 w-full overflow-visible">
      <div
        className="
          grid 
          grid-cols-3 sm:grid-cols-4 md:grid-cols-5 lg:grid-cols-5 
          gap-3
          px-10              /* 🔹 좌우 패딩 추가 (잘림 방지) */
          overflow-visible
          justify-items-center
        "
      >
        {[...Array(5)].map((_, i) => (
          <div
            key={i}
            style={{
              transform: rotations[i],
              transition: "transform 0.2s ease",
              transformOrigin: "center bottom",
            }}
            className="w-fit pointer-events-none"
          >
            <PolaroidCard src={imgSrc} />
          </div>
        ))}
      </div>
    </div>
  );
}
