"use client";

import { useState, useRef, useEffect } from "react";
import { useSearchParams } from "next/navigation";
import PolaroidDetail from "../categories/[id]/components/PolaroidDetail";
import PolaroidCard from "../home/components/PolaroidCard";
import { Send } from "lucide-react";
import { ragSearch } from "@/services/ragService";

interface ChatMessage {
  role: "user" | "assistant";
  content: string;
}

// 카드 그룹 (assistant 답변 1개에서 생성된 카드 리스트)
interface CardGroup {
  answerIndex: number; // messages[] 의 index
  cards: {
    id: number;
    src: string;
    context: string;
    type: string;
    ocr_text: string;
    tags: string[];
    rotate: number; // 랜덤 회전 고정
  }[];
}

export default function RAGSearchPage() {
  const params = useSearchParams();
  const initialQuery = params.get("query") ?? "";

  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [cardGroups, setCardGroups] = useState<CardGroup[]>([]);
  const [query, setQuery] = useState("");
  const [detail, setDetail] = useState<any | null>(null);
  const [loading, setLoading] = useState(false);
  const [layoutExpanded, setLayoutExpanded] = useState(false);

  const scrollRef = useRef<HTMLDivElement>(null);
  const hasRun = useRef(false);

  useEffect(() => {
    if (detail && !layoutExpanded) {
      // detail이 처음 세팅된 순간
      setLayoutExpanded(true);
    }
  }, [detail]);

  /** 자동 스크롤 */
  useEffect(() => {
    scrollRef.current?.scrollTo({
      top: scrollRef.current.scrollHeight,
      behavior: "smooth",
    });
  }, [messages, cardGroups]);

  /** 첫 진입 시 자동 검색 */
  useEffect(() => {
    if (hasRun.current) return;
    hasRun.current = true;

    if (!initialQuery) return;

    setMessages([{ role: "user", content: initialQuery }]);
    performSearch(initialQuery);
  }, [initialQuery]);

  /** 검색 실행 */
  const performSearch = async (text: string) => {
    setLoading(true);

    try {
      const data = await ragSearch(text);
      const { answer, sources } = data;

      // AI 답변 추가
      setMessages((prev) => [...prev, { role: "assistant", content: answer }]);

      const answerIndex = messages.length; // 방금 추가될 assistant message index

      // 카드 rotate 값을 생성하며 저장
      const newCards = (sources || []).map((s: any) => ({
        id: s.id,
        src: s.src,
        context: s.context,
        type: s.type,
        ocr_text: s.ocr_text,
        tags: s.tags || [],
        rotate: Math.random() * 8 - 4, // -4 ~ +4도
      }));

      // 카드 그룹 추가
      setCardGroups((prev) => [...prev, { answerIndex, cards: newCards }]);

      // detail 초기 설정
      if (newCards.length > 0) {
        const c = newCards[0];
        setDetail({
          id: c.id,
          src: c.src,
          tags: c.tags.map((t: string) => `#${t}`),
          contexts: c.context,
          date: "",
          favorite: false,
          type: c.type,
          ocr_text: c.ocr_text,
        });
      }
    } catch (err) {
      console.error(err);
      setMessages((prev) => [
        ...prev,
        { role: "assistant", content: "AI 검색 중 오류가 발생했습니다." },
      ]);
    } finally {
      setLoading(false);
    }
  };

  /** 유저 입력 전송 */
  const handleSend = () => {
    const text = query.trim();
    if (!text) return;

    setMessages((prev) => [...prev, { role: "user", content: text }]);
    setQuery("");
    performSearch(text);
  };

  return (
    <div className="w-full h-[calc(100vh-100px)] flex justify-center bg-[#FFFEF8] text-[#4C3D25] overflow-hidden">
      <div
        className={`h-full flex flex-row gap-6 pb-6 pl-6 transition-all duration-500`}
        style={{
          width: layoutExpanded ? "1200px" : "720px",
        }}
      >
        {/* LEFT AREA */}
        <div
          className="flex-1 h-full flex flex-col transition-all duration-500"
          style={{
            width: layoutExpanded ? "720px" : "720px", // 폭은 같지만 위치가 움직임
          }}
        >
          <div className="flex flex-col bg-[#F4EFE2] rounded-2xl shadow-sm flex-1 overflow-hidden">
            <div
              ref={scrollRef}
              className="flex-1 overflow-y-auto p-6 pr-2 scrollbar-thin scrollbar-thumb-[#CBBF9E]/50"
            >
              {/* 메시지 + 카드 그룹 렌더링 */}
              {messages.map((msg, i) => (
                <div key={i} className="mb-8">
                  {/* ====== 메시지 ====== */}
                  <div
                    className={`flex mb-4 ${
                      msg.role === "user" ? "justify-end" : "justify-start"
                    }`}
                  >
                    {msg.role === "assistant" && (
                      <div className="w-10 h-10 rounded-full overflow-hidden mr-3">
                        <img
                          src="/images/POLA_chatbot.png"
                          className="w-full h-full object-cover"
                        />
                      </div>
                    )}

                    <div
                      className={`max-w-[70%] p-4 rounded-2xl text-base leading-relaxed shadow-sm ${
                        msg.role === "user"
                          ? "bg-[#4C3D25] text-white"
                          : "bg-white text-[#4C3D25] border border-[#E3DCC8]"
                      }`}
                    >
                      {msg.content}
                    </div>
                  </div>

                  {/* ====== 해당 메시지에 해당하는 카드 그룹 ====== */}
                  {cardGroups
                    .filter((g) => g.answerIndex === i)
                    .map((group, gi) => (
                      <div
                        key={gi}
                        className="grid grid-cols-2 gap-4 ml-14 mt-3 w-[330px]"
                        /* 330px 정도면 폴라로이드 2×2가 딱 맞음 */
                      >
                        {group.cards.map((c, idx) => (
                          <div
                            key={idx}
                            onClick={() =>
                              setDetail({
                                id: c.id,
                                src: c.src,
                                tags: c.tags.map((t: string) => `#${t}`),
                                contexts: c.context,
                                date: "",
                                favorite: false,
                                type: c.type,
                                ocr_text: c.ocr_text,
                              })
                            }
                            className="cursor-pointer transition-transform duration-200 hover:scale-105"
                            style={{
                              transform: `rotate(${c.rotate}deg)`,
                            }}
                          >
                            <PolaroidCard
                              src={c.src}
                              type={c.type}
                              ocr_text={c.ocr_text}
                            />
                          </div>
                        ))}
                      </div>
                    ))}
                </div>
              ))}

              {loading && (
                <div className="text-center text-sm text-[#7A6A48] mt-2">
                  AI가 답변을 생성하는 중...
                </div>
              )}
            </div>

            {/* 입력창 */}
            <div className="p-4 flex items-center gap-2 border-t border-[#E5DECF] bg-[#F4EFE2]">
              <input
                type="text"
                placeholder="AI에게 질문해보세요..."
                className="flex-grow bg-white border border-[#D8D5CC] rounded-full px-4 py-3 outline-none"
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                onKeyDown={(e) => e.key === "Enter" && handleSend()}
              />

              <button
                onClick={handleSend}
                className="w-12 h-12 flex items-center justify-center rounded-full bg-[#4C3D25] text-white hover:bg-[#3A311F]"
              >
                <Send className="w-5 h-5" />
              </button>
            </div>
          </div>
        </div>

        {/* RIGHT: DETAIL */}
        <div
          className={`flex-shrink-0 border-l border-[#E3DCC8] flex justify-center pt-8 overflow-y-auto transition-all duration-500`}
          style={{
            width: layoutExpanded ? "480px" : "0px",
            opacity: layoutExpanded ? 1 : 0,
            pointerEvents: layoutExpanded ? "auto" : "none",
          }}
        >
          {layoutExpanded && detail && <PolaroidDetail {...detail} />}
        </div>
      </div>
    </div>
  );
}
