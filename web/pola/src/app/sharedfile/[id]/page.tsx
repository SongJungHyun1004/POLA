import { serverApiClient } from "@/api/serverApiClient";
import PolaroidDetail from "@/app/categories/[id]/components/PolaroidDetail";
import CryptoJS from "crypto-js";
import { redirect } from "next/navigation";

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
  let encrypted = q.user ?? "";

  let username = "알 수 없음";

  if (encrypted) {
    try {
      const bytes = CryptoJS.AES.decrypt(
        decodeURIComponent(encrypted),
        process.env.NEXT_PUBLIC_SHARE_KEY!
      );
      const decoded = bytes.toString(CryptoJS.enc.Utf8);

      if (decoded) username = decoded;
    } catch {
      username = "알 수 없음";
    }
  }

  if (username === "fired") {
    redirect(`/sharedfile/fired`);
  }

  const num = Number(id);

  let fileData: any = null;
  try {
    const res = await serverApiClient(`/files/${num}`, { method: "GET" });
    const json = await res.json();
    fileData = json.data;
  } catch (e) {
    console.error("파일 조회 실패:", e);
    fileData = null;
  }

  if (!fileData) {
    return (
      <main className="min-h-screen flex flex-col items-center justify-center bg-[#FFFEF8] text-[#4C3D25] px-4 py-10">
        <div className="text-xl text-red-600">
          파일 정보를 불러올 수 없습니다.
        </div>
      </main>
    );
  }

  const detail = {
    id: fileData.id,
    src: fileData.src ?? "/images/dummy_image_1.png",
    tags: (fileData.tags ?? []).map((t: any) => `#${t.tagName}`),
    contexts: fileData.context ?? "",
    date: fileData.created_at,
  };

  return (
    <main className="min-h-screen flex flex-col items-center justify-center bg-[#FFFEF8] text-[#4C3D25] px-4 py-10">
      <PolaroidDetail
        id={detail.id}
        src={detail.src}
        tags={detail.tags}
        date={detail.date}
        contexts={detail.contexts}
        sharedView
      />

      {/* 공유자 표시 */}
      <div className="mt-8 text-lg sm:text-xl text-[#333]">
        <span className="font-medium">[{username}]</span>님이 공유한 컨텐츠
      </div>
    </main>
  );
}
