import Header from "./components/Header";
import CategoryBox from "./components/CategoryBox";
import Timeline from "./components/Timeline";
import CategoryRow from "./components/CategoryRow";

export default function HomePage() {
  return (
    <main className="min-h-screen text-[#4C3D25] px-8 py-6 bg-[#FFFEF8]">
      <Header />

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 mt-20">
        <div className="flex flex-col items-center justify-start gap-12 w-full mt-12">
          <div className="flex justify-center gap-10 w-full">
            <CategoryBox text="Favorite" />
            <div className="flex w-8" />
            <CategoryBox text="Remind" />
          </div>

          <div className="flex justify-center w-full">
            <Timeline />
          </div>
        </div>

        <div className="flex flex-col gap-10 overflow-x-hidden overflow-y-auto max-h-[80vh] pr-2 w-full">
          {["Travel", "Food", "Daily", "Friends", "Memories"].map(
            (category) => (
              <CategoryRow
                key={category}
                title={category}
                imgSrc="/images/POLA_logo_1.png"
              />
            )
          )}
        </div>
      </div>
    </main>
  );
}
