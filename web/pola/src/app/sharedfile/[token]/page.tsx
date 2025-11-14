import { getSharedFileByToken } from "@/services/shareService";
import PolaroidDetail from "@/app/categories/[id]/components/PolaroidDetail";
import { redirect } from "next/navigation";

type Params = { token: string };

export default async function SharedFilePage({
  params,
}: {
  params: Promise<Params>;
}) {
  const { token } = await params;

  let sharedFile;
  try {
    sharedFile = await getSharedFileByToken(token);
  } catch (e: any) {
    console.error("공유 파일 조회 실패:", e);

    if (e.message === "EXPIRED_SHARE") {
      redirect("/sharedfile/fired");
    }

    return (
      <main className="min-h-screen flex flex-col items-center justify-center bg-[#FFFEF8] text-[#4C3D25] px-4 py-10">
        <div className="text-xl text-red-600">
          파일 정보를 불러올 수 없습니다.
        </div>
      </main>
    );
  }

  const detail = {
    id: sharedFile.fileId,
    src: sharedFile.presignedUrl ?? "/images/dummy_image_1.png",
    tags: sharedFile.tags?.map((t) => `#${t}`) ?? [],
    contexts: sharedFile.context ?? "",
    date: sharedFile.createdAt,
    platform: sharedFile.platform,
  };

  return (
    <main className="min-h-screen flex flex-col items-center justify-center bg-[#FFFEF8] text-[#4C3D25] px-4 py-10">
      <div className="mb-4 text-lg sm:text-xl text-[#333]">
        <span className="font-medium">[{sharedFile.ownerName}]</span>님이 공유한
        컨텐츠
      </div>
      <PolaroidDetail
        id={detail.id}
        src={detail.src}
        tags={detail.tags}
        date={detail.date}
        contexts={detail.contexts}
        platform={detail.platform}
        sharedView
        downloadUrl={sharedFile.downloadUrl}
      />
    </main>
  );
}
