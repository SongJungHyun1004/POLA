import Header from "./components/Header";
import CategoryBox from "./components/CategoryBox";
import Timeline from "./components/Timeline";
import CategoryRow from "./components/CategoryRow";
import TextLink from "./components/TextLink";

export default function HomePage() {
  return (
    <main className="min-h-screen text-[#4C3D25] px-8 py-6 bg-[#FFFEF8]">
      <Header />

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 mt-20">
        {/* 좌측 영역 */}
        <div className="flex flex-col items-center justify-start gap-12 w-full mt-16">
          <div className="flex justify-center gap-10 w-full">
            <CategoryBox text="Favorite" />
            <div className="flex w-8" />
            <CategoryBox text="Remind" />
          </div>

          <div className="flex flex-col items-center justify-center w-full mt-8">
            <Timeline />
            <TextLink text="Timeline" />
          </div>
        </div>

        {/* 우측 영역 */}
        <div className="flex flex-col gap-8 overflow-x-hidden overflow-y-auto max-h-[76vh] pr-2 w-full">
          {["Travel", "Food", "Daily", "Friends", "Memories"].map(
            (category) => {
              return (
                <div key={category} className="w-full">
                  <TextLink text={category} />
                  <CategoryRow imgSrc="/images/POLA_logo_1.png" />
                </div>
              );
            }
          )}
        </div>
      </div>
    </main>
  );
}
