"use client";

import { Suspense } from "react";
import FilesPageContent from "./FilesPageContent";

export default function FilesPage() {
  return (
    <Suspense
      fallback={
        <div className="p-8 text-center text-[#4C3D25]">Loading...</div>
      }
    >
      <FilesPageContent />
    </Suspense>
  );
}
