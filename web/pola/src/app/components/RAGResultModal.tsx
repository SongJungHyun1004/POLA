"use client";

import { useEffect, useState } from "react";
import ChatBubble from "./ChatBubble";
import ChatInput from "./ChatInput";
import SourcePolaroidGrid from "./SourcePolaroidGrid";
import PolaroidDetail from "../(pola)/categories/[id]/components/PolaroidDetail";

interface RAGResultModalProps {
  onClose: () => void;
  query: string;
  data?: {
    answer: string;
    sources: any[];
  };
}

export default function RAGResultModal({
  onClose,
  query,
  data,
}: RAGResultModalProps) {
  const [visible, setVisible] = useState(false);

  // mount 시 fade-in 효과
  useEffect(() => {
    const t = setTimeout(() => setVisible(true), 10);
    return () => clearTimeout(t);
  }, []);

  return (
    <div
      className={`fixed inset-0 z-50 py-6 flex justify-center items-start bg-black/30 backdrop-blur-sm transition-opacity duration-200 ${
        visible ? "opacity-100" : "opacity-0"
      }`}
      onClick={onClose}
    >
      <div
        onClick={(e) => e.stopPropagation()}
        className={`relative bg-white w-3/4 h-full py-6 border rounded-2xl shadow-2xl flex transition-transform duration-300 ${
          visible ? "translate-y-0" : "-translate-y-4"
        }`}
      >
        {/* 좌측 채팅 영역 */}
        <div className="w-[60%] flex flex-col justify-between px-8 border-r border-[#E3DCC8]">
          <div className="flex-1 overflow-y-auto space-y-6 pr-2">
            <ChatBubble role="user" text={query} />
            {data?.answer && <ChatBubble role="ai" text={data.answer} />}
            {data?.sources && data.sources.length > 0 && (
              <SourcePolaroidGrid sources={data.sources} />
            )}
          </div>

          {/* 입력창 */}
          <div className="pt-4 border-t border-[#E3DCC8]">
            <ChatInput placeholder="AI에게 질문하기..." />
          </div>
        </div>

        {/* 우측 디테일 영역 */}
        <div className="flex-1 px-6 overflow-y-auto pt-4">
          {data?.sources?.[0] && (
            <PolaroidDetail
              id={data.sources[0].id}
              src={data.sources[0].src}
              tags={data.sources[0].tags}
              contexts={data.sources[0].context}
              date={undefined}
              favorite={false}
              platform={data.sources[0].platform}
            />
          )}
        </div>
      </div>
    </div>
  );
}
