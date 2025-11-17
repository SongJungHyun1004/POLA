import { Suspense } from "react";
import RAGSearchPageInner from "./RAGSearchPageInner";

export default function Page() {
  return (
    <Suspense fallback={<div />}>
      <RAGSearchPageInner />
    </Suspense>
  );
}
