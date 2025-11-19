import { Suspense } from "react";
import RAGSearchPageInner from "./RAGSearchPageInner";

export default function Page() {
  return (
    <Suspense
      fallback={
        <div className="p-8 text-center text-[#4C3D25]">Loading...</div>
      }
    >
      <RAGSearchPageInner />
    </Suspense>
  );
}
