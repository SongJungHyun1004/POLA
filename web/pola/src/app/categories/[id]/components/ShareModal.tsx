"use client";

import { useEffect, useState } from "react";
import { Copy, Check, X } from "lucide-react";
import { createFileShareLink } from "@/services/fileService";

interface ShareModalProps {
  id: number;
  onClose: () => void;
}

export default function ShareModal({ id, onClose }: ShareModalProps) {
  const [copied, setCopied] = useState(false);
  const [loading, setLoading] = useState(true);
  const [shareUrl, setShareUrl] = useState("");

  useEffect(() => {
    async function fetchShareUrl() {
      try {
        const { shareUrl } = await createFileShareLink(id);
        const origin = window.location.origin;
        setShareUrl(`${origin}/sharedfile/${shareUrl}`);
      } catch (e) {
        console.error("공유 링크 생성 실패:", e);
        alert("공유 링크 생성에 실패했습니다.");
      } finally {
        setLoading(false);
      }
    }
    fetchShareUrl();
  }, [id]);

  useEffect(() => {
    const onKey = (e: KeyboardEvent) => e.key === "Escape" && onClose();
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [onClose]);

  const copyToClipboard = async () => {
    if (!shareUrl) return;
    try {
      await navigator.clipboard.writeText(shareUrl);
      setCopied(true);
      setTimeout(() => setCopied(false), 1200);
    } catch {
      alert("클립보드 복사에 실패했습니다.");
    }
  };

  if (loading) {
    return (
      <div
        className="fixed inset-0 z-[60] bg-black/60 backdrop-blur-sm flex items-center justify-center"
        onClick={onClose}
      >
        <div
          className="w-[520px] max-w-[92vw] bg-white rounded-xl shadow-lg p-6 text-center text-[#4C3D25]"
          onClick={(e) => e.stopPropagation()}
        >
          <p className="text-base">공유 링크 생성 중...</p>
        </div>
      </div>
    );
  }

  return (
    <div
      className="fixed inset-0 z-[60] bg-black/60 backdrop-blur-sm flex items-center justify-center"
      onClick={onClose}
    >
      <div
        className="w-[520px] max-w-[92vw] bg-white rounded-xl shadow-lg p-5"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-center justify-between mb-3">
          <h3 className="text-lg font-semibold text-[#4C3D25]">공유 링크</h3>
          <button className="p-1 rounded hover:bg-black/5" onClick={onClose}>
            <X className="w-5 h-5 text-[#4C3D25]" />
          </button>
        </div>

        <div className="flex gap-2">
          <input
            value={shareUrl}
            readOnly
            onClick={() => window.open(shareUrl, "_blank")}
            className="flex-1 border rounded-md px-3 py-2 text-sm text-[#4C3D25] bg-[#FFFEF8] select-all cursor-pointer"
          />
          <button
            onClick={copyToClipboard}
            className="flex items-center gap-1 border rounded-md px-3 py-2 text-sm hover:bg-[#F6F1E7]"
          >
            {copied ? (
              <>
                <Check className="w-4 h-4" />
                복사됨
              </>
            ) : (
              <>
                <Copy className="w-4 h-4" />
                복사
              </>
            )}
          </button>
        </div>

        <p className="mt-3 text-xs text-[#7A6A48]">
          이 링크를 공유하면 누구나 해당 폴라로이드를 24시간 동안 볼 수
          있습니다.
        </p>
      </div>
    </div>
  );
}
