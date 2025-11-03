import PolaroidDetail from "@/app/category/[id]/components/PolaroidDetail";
import Image from "next/image";

type Params = { id: string };
type Search = { user?: string };

export default async function SharedFilePage({
  params,
  searchParams,
}: {
  params: Promise<Params>;
  searchParams?: Promise<Search>;
}) {
  const { id } = await params;
  const q = (await searchParams) || {};
  const username = q.user ?? "username";

  const num = Number(id);
  const images = {
    id: num,
    src: "/images/dummy_image_1.png",
    tags: ["#태그1", "#태그2", "#태그3", "#태그4", "#태그5", "#태그6"],
    contexts: "내용을 입력하세요...",
    date: "2025.10.30",
  };

  return (
    <main className="min-h-screen flex flex-col items-center justify-center bg-[#FFFEF8] text-[#4C3D25] px-4 py-10">
      <PolaroidDetail
        id={num}
        src={images.src}
        tags={images.tags}
        date={images.date}
        contexts={images.contexts}
        username={username}
        sharedView
      />

      {/* 공유자 표시 */}
      <div className="mt-8 text-lg sm:text-xl text-[#333]">
        <span className="font-medium">[{username}]</span>님이 공유한 컨텐츠
      </div>
    </main>
  );
}
