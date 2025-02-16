import { createRoot } from "react-dom/client";
import { useState } from "react";
import ProfileCreator from "./ProfileCreator";

const App = () => {
  return (
    <div>
      <ProfileCreator />
    {/* <Counter /> */}
    </div>
  );
};

const container = document.getElementById("root");
const root = createRoot(container);
root.render(<App />);

// const Counter = () => {
//   const [count, setCount] = useState(0);
//   const [name, setName] = useState();

//   return <div>Counter
//     <form>
//       <input type="text" value={name} onChange={(e) => setName(e.target.value)} />
//     </form>
//     <button onClick={() => setCount(count + 1)} >+</button>
//     <button onClick={() => setCount(count - 1)}>-</button>
//     <p>{count}</p>
//     <p>Name is {name}</p>
//   </div>;
// };
