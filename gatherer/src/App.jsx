import { createRoot } from "react-dom/client";
import ProfileCreator from "./ProfileCreator";

const App = () => {
  return (
    <div>
      <ProfileCreator />
    </div>
  );
};

const container = document.getElementById("root");
const root = createRoot(container);
root.render(<App />);
