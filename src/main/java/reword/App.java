package reword;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

public class App {
    public static void main(String[] args) throws IOException
    {
        String pathToGitFolder = ".git";
        String inputCommit = "8bee2f73c07e539aaca22211f3f2955e2769be5c";
        
        Repository repository = new FileRepositoryBuilder()
        .setGitDir(new File(pathToGitFolder))
        .build();
    
        Ref head = repository.exactRef("refs/heads/master");

        ObjectId toCommitId = head.getObjectId();
        String to = toCommitId.getName();

        ObjectId fromCommitId = repository.resolve(inputCommit);
        String from = fromCommitId.getName();

        RevWalk walk = new RevWalk(repository);

        RevCommit commit = walk.parseCommit(repository.resolve(to));

        System.out.println("Start-Commit: " + commit + "\n");
        System.out.println("Walking all commits starting at " + to + " until we find " + from + "\n");

        walk.markStart(commit);

        List<String> commitsContent = new ArrayList<String>();
        int count = 0;

        for (RevCommit rev : walk) {
            System.out.println("Commit: " + rev);
            count++;

            commitsContent.add(new String(repository.open(rev).getBytes()));

            if (rev.getId().getName().equals(from)) {
                System.out.println("\nFound from, stopping walk. " + count);
                break;
            }
        }

        walk.dispose();
        walk.close();
    }
}